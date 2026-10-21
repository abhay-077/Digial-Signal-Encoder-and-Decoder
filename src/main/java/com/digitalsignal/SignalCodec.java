package com.digitalsignal;

import java.util.ArrayList;
import java.util.List;

/**
 * Line-code codec. Each input bit is represented by two half-bit samples.
 * NRZ-L: 0=+1, 1=-1. NRZ-I starts at +1 and transitions at the start of a 1.
 * Manchester: 0=+1,-1 and 1=-1,+1. Differential Manchester starts at +1;
 * 0 has a boundary transition and every bit has a mid-bit transition.
 */
public final class SignalCodec {
    public static final List<String> SCHEMES = List.of(
            "NRZ-L", "NRZ-I", "Manchester", "Differential Manchester",
            "AMI", "B8ZS", "HDB3", "RZ");

    private SignalCodec() {}

    public static List<Double> encode(String bits, String type) {
        validateBits(bits);
        if (type == null || !SCHEMES.contains(type))
            throw new IllegalArgumentException("Unsupported encoding: " + type);
        return switch (type) {
            case "NRZ-L" -> nrzl(bits);
            case "NRZ-I" -> nrzi(bits);
            case "Manchester" -> manchester(bits);
            case "Differential Manchester" -> differentialManchester(bits);
            case "AMI" -> ami(bits);
            case "B8ZS" -> scramble(bits, true);
            case "HDB3" -> scramble(bits, false);
            case "RZ" -> rz(bits);
            default -> throw new IllegalArgumentException("Unsupported encoding: " + type);
        };
    }

    public static String decode(List<Double> signal, String type) {
        if (signal == null || signal.isEmpty() || signal.size() % 2 != 0)
            throw new IllegalArgumentException("Signal must contain two samples per bit.");
        if (type == null || !SCHEMES.contains(type))
            throw new IllegalArgumentException("Unsupported encoding: " + type);
        int n = signal.size() / 2;
        StringBuilder out = new StringBuilder(n);
        double previous = 1.0;
        int lastNonZero = -1;
        int pulseCount = 0;
        for (int i = 0; i < n; i++) {
            double a = clean(signal.get(i * 2)), b = clean(signal.get(i * 2 + 1));
            char bit;
            switch (type) {
                case "NRZ-L" -> bit = a > 0 ? '0' : '1';
                case "NRZ-I" -> {
                    bit = a != previous ? '1' : '0';
                    previous = b;
                }
                case "Manchester" -> {
                    if (a == b) throw new IllegalArgumentException("Invalid Manchester transition at bit " + i);
                    bit = a > b ? '0' : '1';
                }
                case "Differential Manchester" -> {
                    if (a == b) throw new IllegalArgumentException("Invalid differential-Manchester mid-bit transition at bit " + i);
                    bit = a == previous ? '1' : '0';
                    previous = b;
                }
                case "AMI" -> {
                    bit = (a == 0 && b == 0) ? '0' : '1';
                    if (a != b) throw new IllegalArgumentException("Invalid AMI pulse at bit " + i);
                }
                case "RZ" -> bit = a < 0 ? '0' : '1';
                case "B8ZS", "HDB3" -> {
                    // Scrambled AMI uses identical levels in each bit cell.
                    if (a != b) throw new IllegalArgumentException("Invalid scrambled-AMI cell at bit " + i);
                    if (a == 0) {
                        bit = '0';
                    } else {
                        bit = '1';
                        // B8ZS violation pattern 000VB0VB; HDB3 patterns 000V or B00V
                        // are recognized below using the encoded pulse stream.
                    }
                }
                default -> throw new IllegalArgumentException("Unsupported encoding: " + type);
            }
            out.append(bit);
        }
        if (type.equals("B8ZS")) undoB8zs(signal, out);
        if (type.equals("HDB3")) undoHdb3(signal, out);
        return out.toString();
    }

    private static List<Double> nrzl(String bits) {
        List<Double> s = new ArrayList<>(bits.length()*2);
        for (char c: bits.toCharArray()) add(s, c=='0'?1:-1, c=='0'?1:-1);
        return s;
    }
    private static List<Double> nrzi(String bits) {
        List<Double> s = new ArrayList<>(bits.length()*2); double level=1;
        for (char c: bits.toCharArray()) { if(c=='1') level=-level; add(s,level,level); }
        return s;
    }
    private static List<Double> manchester(String bits) {
        List<Double> s = new ArrayList<>(bits.length()*2);
        for(char c:bits.toCharArray()) add(s,c=='0'?1:-1,c=='0'?-1:1);
        return s;
    }
    private static List<Double> differentialManchester(String bits) {
        List<Double> s = new ArrayList<>(bits.length()*2); double level=1;
        for(char c:bits.toCharArray()) { if(c=='0') level=-level; s.add(level); level=-level; s.add(level); }
        return s;
    }
    private static List<Double> ami(String bits) {
        List<Double> s = new ArrayList<>(bits.length()*2); double polarity=1;
        for(char c:bits.toCharArray()) { double v=0; if(c=='1'){v=polarity;polarity=-polarity;} add(s,v,v); }
        return s;
    }
    private static List<Double> rz(String bits) {
        List<Double> s = new ArrayList<>(bits.length()*2);
        for(char c:bits.toCharArray()) { s.add(c=='1'?1.0:-1.0); s.add(0.0); }
        return s;
    }

    private static List<Double> scramble(String bits, boolean b8zs) {
        List<Double> s = new ArrayList<>(bits.length()*2);
        List<Double> pulses = new ArrayList<>(bits.length());
        double last = -1; int onesSinceViolation = 0;
        for (int i=0; i<bits.length();) {
            if (bits.charAt(i)=='1') {
                last=-last; pulses.add(last); onesSinceViolation++; i++; continue;
            }
            int zeros=0; while(i+zeros<bits.length() && bits.charAt(i+zeros)=='0') zeros++;
            int run = b8zs ? 8 : 4;
            if(zeros < run) { for(int j=0;j<zeros;j++) pulses.add(0.0); i+=zeros; continue; }
            if(b8zs) {
                // 000VB0VB: V has same polarity as preceding non-zero; B is normal AMI.
                double v=last, b=-last;
                double[] pat={0,0,0,v,b,0,b,v};
                for(double p:pat) pulses.add(p);
                last=v; i+=8;
            } else {
                // HDB3: 000V if odd number of nonzero pulses since last violation;
                // B00V if even. V violates AMI, B is a normal AMI pulse.
                boolean odd=(onesSinceViolation%2)==1;
                double v=last;
                double[] pat;
                if(odd) pat=new double[]{0,0,0,v};
                else pat=new double[]{-last,0,0,-last};
                for(double p:pat) pulses.add(p);
                last=pat[3];
                onesSinceViolation=0;
                i+=4;
            }
        }
        for(double p:pulses) add(s,p,p);
        return s;
    }

    private static void undoB8zs(List<Double> signal, StringBuilder out) {
        // Replace the exact 000VB0VB violation pattern by eight zeros.
        for(int i=0;i+7<out.length();i++) {
            if(out.substring(i,i+8).equals("00011011")) {
                // Pattern detection must use polarity, not decoded pulse presence.
                double p=signal.get(i*2+8);
                double v0=signal.get(i*2+6), b1=signal.get(i*2+8), b2=signal.get(i*2+10), v2=signal.get(i*2+14);
                if(v0!=0 && b1==-v0 && b2==-v0 && v2==v0) { out.replace(i,i+8,"00000000"); i+=7; }
            }
        }
        // Robust polarity-based recognition across either initial polarity.
        for(int i=0;i+7<out.length();i++) {
            double a=signal.get(i*2), b=signal.get((i+1)*2), c=signal.get((i+2)*2), d=signal.get((i+3)*2);
            double e=signal.get((i+4)*2), f=signal.get((i+5)*2), g=signal.get((i+6)*2), h=signal.get((i+7)*2);
            if(a==0&&b==0&&c==0&&d!=0&&e==-d&&f==0&&g==-d&&h==d) out.replace(i,i+8,"00000000");
        }
    }
    private static void undoHdb3(List<Double> signal, StringBuilder out) {
        // Detect 000V and B00V substitutions from violation pulse polarity.
        for(int i=0;i+3<out.length();i++) {
            double a=signal.get(i*2), b=signal.get((i+1)*2), c=signal.get((i+2)*2), d=signal.get((i+3)*2);
            if(a==0&&b==0&&c==0&&d!=0) {
                double prev=previousNonZero(signal,i);
                if(d==prev) out.replace(i,i+4,"0000");
            } else if(a!=0&&b==0&&c==0&&d!=0&&a==d) {
                double prev=previousNonZero(signal,i);
                if(prev==0||a!=prev) out.replace(i,i+4,"0000");
            }
        }
    }
    private static double previousNonZero(List<Double> s,int bitIndex) {
        for(int i=bitIndex-1;i>=0;i--) { double v=s.get(i*2); if(v!=0)return v; }
        return 0;
    }
    private static double clean(Double value) {
        if(value==null || !Double.isFinite(value)) throw new IllegalArgumentException("Signal has an invalid sample.");
        if(Math.abs(value)<1e-9)return 0;
        return value>0?1:-1;
    }
    private static void add(List<Double> s,double a,double b){s.add(a);s.add(b);}
    private static void validateBits(String bits) {
        if(bits==null||bits.isEmpty()||!bits.matches("[01]+"))
            throw new IllegalArgumentException("Input must contain only 0s and 1s.");
    }
}
