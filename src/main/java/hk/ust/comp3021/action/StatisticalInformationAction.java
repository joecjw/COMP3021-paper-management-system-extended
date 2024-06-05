package hk.ust.comp3021.action;

import hk.ust.comp3021.resource.Paper;
import hk.ust.comp3021.person.User;
import java.util.*;
import java.util.function.Function;

public class StatisticalInformationAction extends Action {
    public enum InfoKind {
        AVERAGE,
        MAXIMAL,
    };

    private InfoKind kind;

    private final Map<String, Double> actionResult = new HashMap<String, Double>();

    public StatisticalInformationAction(String id, User user, Date time, InfoKind kind) {
        super(id, user, time, ActionType.STATISTICAL_INFO);
        this.kind = kind;
    }

    public InfoKind getKind() {
        return kind;
    }

    public void setKind(InfoKind kind) {
        this.kind = kind;
    }

    public Map<String, Double> getActionResult() {
        return actionResult;
    }

    public void appendToActionResult(String key, Double value) {
        this.actionResult.put(key, value);
    }

    /**
     * TODO `obtainer1` indicates the first profiling criterion,
     *    i.e., Obtain the average number of papers published by researchers per year.
     * @param a list of papers to be profiled
     * @return `actionResult` that contains the target result
     */
    public Function<List<Paper>, Map<String, Double>> obtainer1 = paperList -> {
        Map<String,Double> nums = new HashMap<>();
        Map<String,List<Integer>> years = new HashMap<>();
        paperList.forEach(paper -> {
            paper.getAuthors().forEach(author -> {
                if(nums.isEmpty() || !nums.containsKey(author)){
                    nums.put(author,1.0);
                    List<Integer> t = new ArrayList<>();
                    t.add(paper.getYear());
                    years.put(author,t);
                } else if (!nums.isEmpty() && nums.containsKey(author)) {
                    nums.replace(author, nums.get(author)+1);
                    if(!years.get(author).contains(paper.getYear())){
                        List<Integer> t = new ArrayList<>();
                        t.addAll(years.get(author));
                        t.add(paper.getYear());
                        years.replace(author,t);
                    }
                }
            });
        });
        nums.forEach((name, num)->{
           this.actionResult.put(name, num / years.get(name).size());
        });

        return this.actionResult;
    };

    /**
     * TODO `obtainer2` indicates the second profiling criterion,
     *    i.e., Obtain the journals that receive the most papers every year.
     * @param a list of papers to be profiled
     * @return `actionResult` that contains the target result
     * PS1: If two journals receive the same number of papers in a given year, then we take the default order.
     * PS2: We keep the chronological order of year so that the results of the subsequent year will replace the
     *       results of the previous year if one journal receives the most papers in two or more different years.
     */
    public Function<List<Paper>, Map<String, Double>> obtainer2 = paperList -> {
        Map<Integer,Map<String,Double>> yearsTojournalsMap = new HashMap<>();
        paperList.forEach(paper -> {
            if(paper.getJournal() != null && !paper.getJournal().isEmpty()){
                if(yearsTojournalsMap.isEmpty() || !yearsTojournalsMap.containsKey(paper.getYear())){
                    Map<String,Double> journalsToCountsMap = new HashMap<>();
                    journalsToCountsMap.put(paper.getJournal(), 1.0);
                    yearsTojournalsMap.put(paper.getYear(), journalsToCountsMap);
                } else if (!yearsTojournalsMap.isEmpty() && yearsTojournalsMap.containsKey(paper.getYear())) {
                    if(yearsTojournalsMap.get(paper.getYear()).containsKey(paper.getJournal())){
                        yearsTojournalsMap.get(paper.getYear()).replace(paper.getJournal()
                                , yearsTojournalsMap.get(paper.getYear()).get(paper.getJournal())+1.0);
                    } else if (!yearsTojournalsMap.get(paper.getYear()).containsKey(paper.getJournal())) {
                        yearsTojournalsMap.get(paper.getYear()).put(paper.getJournal(), 1.0);
                    }
                }
            }
        });

        yearsTojournalsMap.forEach((year, map)->{
            Optional<Map.Entry<String, Double>> result =  map.entrySet()
                                                             .stream()
                                                             .max(Comparator.comparing(Map.Entry::getValue));
            this.actionResult.put(result.get().getKey(), result.get().getValue());
        });

        return this.actionResult;
    };

}
