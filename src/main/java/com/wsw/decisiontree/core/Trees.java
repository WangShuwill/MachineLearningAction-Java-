package com.wsw.decisiontree.core;

import java.util.*;

/**
 * @author Wangshuwei
 * @since 2017-5-10
 */
public class Trees {

    /**
     * 计算熵
     *
     * @param dataSets 数据集合
     * @return 熵
     */
    public static double calcShnnoEnt(List<List<Object>> dataSets) {
        int numEntires = dataSets.size();
        Map<String, Integer> labelCounts = new HashMap<>();
        for (List<Object> dataSet : dataSets) {
            String currentLabel = (String) dataSet.get(dataSet.size() - 1);
            if (!labelCounts.containsKey(currentLabel)) {
                labelCounts.put(currentLabel, 0);
            }
            labelCounts.put(currentLabel, labelCounts.get(currentLabel) + 1);
        }
        double shnnoEnt = 0.0;
        for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
            double prob = (double) entry.getValue() / numEntires;
            shnnoEnt -= prob * (Math.log10(prob) / Math.log10(2));
        }
        return shnnoEnt;
    }

    /**
     * 按照给定特征划分数据集
     *
     * @param dataSet:数据集
     * @param axis:输入值
     * @param value:期望值
     * @return 划分后的数据集
     */
    public static List<List<Object>> splitDataSet(List<List<Object>> dataSet, int axis, int value) {
        List<List<Object>> retDataSet = new ArrayList<>();
        for (List<Object> featVec : dataSet) {
            if ((Integer) featVec.get(axis) == value) {
                List<Object> copyFeatVec = new LinkedList<>(featVec);
                copyFeatVec.remove(axis);
                retDataSet.add(copyFeatVec);
            }
        }
        return retDataSet;
    }

    /**
     * 选择最好的数据集合划分方式
     *
     * @param dataSets:数据集合
     * @return 最好特征值的索引
     */
    public static int chooseBestFeatureToSplit(List<List<Object>> dataSets) {
        int numFeatures = dataSets.get(0).size() - 1;
        double baseEntropy = calcShnnoEnt(dataSets);
        double bestInfoGain = 0.0;
        int bestFeature = -1;
        for (int i = 0; i < numFeatures; i++) {
            Set<Object> set = new HashSet<>();
            for (List<Object> dataSet : dataSets) {
                set.add(dataSet.get(i));
            }
            double newEntropy = 0.0;
            for (Object object : set) {
                List<List<Object>> subDataSet = splitDataSet(dataSets, i, (int) object);
                double prob = subDataSet.size() / (double) dataSets.size();
                newEntropy += prob * calcShnnoEnt(subDataSet);
            }
            double infoGain = baseEntropy - newEntropy; //信息获取量
            //System.out.println(infoGain);
            if (infoGain > bestInfoGain) {
                bestInfoGain = infoGain;
                bestFeature = i;
            }
        }
        return bestFeature;
    }

    /**
     * 生成决策树
     *
     * @param dataSet:数据集
     * @param label       :数据标签
     * @return :Object对象，可转成JSON格式
     */
    public static Object createTree(List<List<Object>> dataSet, List<String> label) {
        List<String> classList = new ArrayList<>();
        dataSet.forEach(item -> classList.add((String) item.get(dataSet.get(0).size() - 1)));
        if (classList.stream().filter(item -> item.equals(classList.get(0))).count() == classList.size()) {
            //System.out.println(classList.get(0));
            return classList.get(0);
        }

        int bestFeature = chooseBestFeatureToSplit(dataSet);
        String bestFeatureLabel = label.get(bestFeature);
        //System.out.println(bestFeatureLabel);
        Map<String, Map> tree = new HashMap<>();
        tree.put(bestFeatureLabel, new HashMap<>());
        label.remove(bestFeature);
        Set<Object> featValues = new HashSet<>();
        dataSet.forEach(item -> featValues.add(item.get(bestFeature)));
        for (Object value : featValues) {
            List<String> subList = new ArrayList<>(label);  //关键步骤，作为原先label的拷贝，到从list删除元素时，不会影响上次递归函数入栈后label
            tree.get(bestFeatureLabel).put(value,
                    createTree(splitDataSet(dataSet, bestFeature, (Integer) value), subList));
        }

        return tree;
    }
}
