package a4.papers.chatfilter.chatfilter.shared;

public class StringSimilarity {

    public static double similarity(String s1, String s2) {
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
        }

        int lengthDiff = longerLength - shorter.length();
        if (lengthDiff > longerLength / 2) {
            return (double)(longerLength - lengthDiff) / longerLength;
        }
        
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    public static int editDistance(String s1, String s2) {
        // Convert to lowercase once
        String s1Lower = s1.toLowerCase();
        String s2Lower = s2.toLowerCase();
        
        int len1 = s1Lower.length();
        int len2 = s2Lower.length();
        
        // Early exit for empty strings
        if (len1 == 0) return len2;
        if (len2 == 0) return len1;
        
        int[] costs = new int[len2 + 1];
        for (int i = 0; i <= len1; i++) {
            int lastValue = i;
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    costs[j] = j;
                } else if (j > 0) {
                    int newValue = costs[j - 1];
                    if (s1Lower.charAt(i - 1) != s2Lower.charAt(j - 1)) {
                        newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                    }
                    costs[j - 1] = lastValue;
                    lastValue = newValue;
                }
            }
            if (i > 0) costs[len2] = lastValue;
        }
        return costs[len2];
    }

}
