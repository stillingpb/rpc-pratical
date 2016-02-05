package test;


import java.util.*;

public class test {
    public static void main(String[] args) {
        String[][] tickets = new String[][]{{"JFK", "SFO"}, {"JFK", "ATL"}, {"SFO", "ATL"}, {"ATL", "JFK"}, {"ATL", "SFO"}};
        List<String> ans = new Solution().findItinerary(tickets);
        System.out.println(ans);

        tickets = new String[][]{{"MUC", "LHR"}, {"JFK", "MUC"}, {"SFO", "SJC"}, {"LHR", "SFO"}};
        ans = new Solution().findItinerary(tickets);
        System.out.println(ans);
    }

    public static class Solution {
        public List<String> findItinerary(String[][] tickets) {
            Map<String, Integer> map = new HashMap<String, Integer>();
            List<List<String>> tos = new ArrayList<List<String>>();
            for (int i = 0, count = 0; i < tickets.length; i++) {
                String from = tickets[i][0];
                String to = tickets[i][1];
                Integer idx = map.get(from);
                if (idx == null) {
                    map.put(from, count++);
                    List<String> tt = new ArrayList<String>();
                    tos.add(tt);
                    tt.add(to);
                } else {
                    List<String> tt = tos.get(idx);
                    tt.add(to);
                }
            }

            for (int i = tos.size() - 1; i >= 0; i--) {
                List<String> tt = tos.get(i);
                Collections.sort(tt, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o2.compareTo(o1);
                    }
                });
            }

            List<String> ans = new ArrayList<String>();
            int end = tos.size();
            String from = "JFK";
            ans.add(from);
            while (end != 0) {
                int idx = map.get(from);
                List<String> tt = tos.get(idx);
                String to = tt.remove(tt.size() - 1);
                ans.add(to);
                from = to;
                if (tos.get(idx).size() == 0) {
                    end--;
                }
            }
            return ans;
        }
    }
}
