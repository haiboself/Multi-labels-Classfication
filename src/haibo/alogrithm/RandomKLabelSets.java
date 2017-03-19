package haibo.alogrithm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import mulan.classifier.MultiLabelLearner;
import mulan.classifier.MultiLabelOutput;
import mulan.classifier.meta.MultiLabelMetaLearner;
import mulan.data.MultiLabelInstances;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/*
 * Random k-labelsets 算法的实现
 * 其中底层的LP单标签分类器使用了开源库Mulan和Weka中的实现
 * MultiLabelMetaLearner是开源库Mulan中的类,定了多标签学习算法的一些基本标准,属性和方法.
 */
public class RandomKLabelSets extends MultiLabelMetaLearner {

	private static final long serialVersionUID = 1L;

	//随机数生成器
    private int seed = 0;
    private Random rnd;	
    
    int modelNum;	//构造的LP分类器的数量,根据经验,设置为标签数量的2倍标注效果最好
    double threshold = 0.5;	//域值
    int subsetSIze = 3;	//标签子集subset大小.根据经验,设置为3标注效果最好.
    
    int[][] subsetSelectLabels;		//存储每个标签子集所选取的标签在Labels中的索引值
    int[][] subsetNoSelectLabels;	//存储每个标签子集没有选取的的标签在Labels中的索引值
    
    MultiLabelLearner[] subsetClassifiers;	//存储每个子标签集所对应的LP单标签分类器
    
    private Remove[] remove;	//weka中的类,主要是在为每个LP分类器生成训练集时使用
    
    HashSet<String> subsetContents;	//存储所有subset.主要用于生成subset时查重,避免生成相同的subset
    
    double[] sumVotes;	//存储投标签Lj票的分类器数量
    double[] lengthVotes;	//存储有资格投Lj票的分类器数量

    //baselearner即为采用的LP分类器实现.
    public RandomKLabelSets(MultiLabelLearner baseLearner) {
        super(baseLearner);
    }


    //重写了其父类MultiLabelMetaLearner中的方法,
    //该方法的作用是:生成modelNum个LP单标签分类器
    public void buildInternal(MultiLabelInstances trainingData) throws Exception {
    	if(numLabels < 3) return;	//如果标签集中标签数量少于三个.直接返回,因为这不是多标签分类任务.
    	
        rnd = new Random(seed);	//利用种子生成随机数生成器

      //构造的LP分类器的数量,根据经验,设置为标签数量的2倍标注效果最好.但是数量不能超过所有可能的数量.
        if (modelNum == 0) {
            modelNum = Math.min(2 * numLabels, binomial(numLabels, subsetSIze));
        }
        subsetSelectLabels = new int[modelNum][subsetSIze];
        subsetNoSelectLabels = new int[modelNum][subsetSIze];
        subsetClassifiers = new MultiLabelLearner[modelNum];
        remove = new Remove[modelNum];

        //存储所有subset.主要用于生成subset时查重,避免生成相同的subset
        subsetContents = new HashSet<String>();
      
        //生成moelNum个LP分类器
        for (int i = 0; i < modelNum; i++) {
            createClassifier(trainingData, i);
        }
    }

    //生成每个LP分类器
    //1:首先从标签集L中随机选取k个不重复的标签组成标签子集Ri
    //2:如果Ri和已生成的subset重复,重新选取,知道生成一个不同的subset
    //3:对于Ri,生成其对应的训练集Di.
    //4:使用Di训练和Ri对应的Lp单标签分类器
    //5:mlTrainData是开源库Mulan对多标签训练集进行封装的一种类型
    private void createClassifier(MultiLabelInstances mlTrainData, int model) throws Exception {
        
        Instances trainData = mlTrainData.getDataSet();
       
       
        boolean[] selected;	//用于单个subset生成中的标签查重
        
        //do while用于subset查重
        do {
            selected = new boolean[numLabels];
            
            //从标签集L中随机选取k个不重复的标签组成标签子集Ri
            for (int i = 0; i< subsetSIze; i++) {
                int randomLabel;
                randomLabel = rnd.nextInt(numLabels);
                //subset中的标签查重
                while (selected[randomLabel] != false) {
                    randomLabel = rnd.nextInt(numLabels);
                }
                selected[randomLabel] = true;	//被选择的标签
                //System.out.println("label: " + randomLabel);
                subsetSelectLabels[model][i] = randomLabel;	//存储选取结果
            }
            Arrays.sort(subsetSelectLabels[model]);	//对随机选取的标签根据其索引值进行排序
            
        } while (subsetContents.add(Arrays.toString(subsetSelectLabels[model])) == false);
       
        subsetNoSelectLabels[model] = new int[numLabels - subsetSIze];
        
        //存储Ri中没有选择的标签的索引,用于生成Di
        int k = 0;
        for (int j = 0; j < numLabels; j++) {
            if (selected[j] == false) {
                subsetNoSelectLabels[model][k] = labelIndices[j];
                k++;
            }
        }
        
        remove[model] = new Remove();
        remove[model].setAttributeIndicesArray(subsetNoSelectLabels[model]);
        remove[model].setInputFormat(trainData);
        remove[model].setInvertSelection(false);
        
        //生成Ri对应的训练集Di
        Instances trainSubset = Filter.useFilter(trainData, remove[model]);

        //生成Ri对应的LP分类器
        subsetClassifiers[model] = getBaseLearner().makeCopy();
        subsetClassifiers[model].build(mlTrainData.reintegrateModifiedDataSet(trainSubset));
    }

    //进行多标签标注,重写了其父类的方法. 标注的方法在于对所有Lp分类器的结果进行组合投票,最终投票过半的标签就被并入最终标注结果.
    //MultiLabelOutput是开源库Mulan多标签算法标注结果的封装类型
    //Instance是开源库Weka中对于一条向量表示的数据的封装类型
    @Override
    protected MultiLabelOutput makePredictionInternal(Instance instance) throws Exception {
    	
        double[] sumConf = new double[numLabels];
        sumVotes = new double[numLabels];	//获得的票数
        lengthVotes = new double[numLabels];

        //遍历所有LP分类器.对同一条实例数据进行预测.
        for (int i = 0; i < modelNum; i++) {
        	
        	//将这一条实例数据处理成分类器LPi所需要的格式.
            remove[i].input(instance);
            remove[i].batchFinished();
            Instance newInstance = remove[i].output();
            
            //进行单标签分类
            MultiLabelOutput subsetRes = subsetClassifiers[i].makePrediction(newInstance);
            
            //进行票数统计
            for (int j = 0; j < subsetSIze; j++) {
                sumConf[subsetSelectLabels[i][j]] += subsetRes.getConfidences()[j];
                //统计投标签Lj票的分类器数量
                sumVotes[subsetSelectLabels[i][j]] += subsetRes.getBipartition()[j] ? 1 : 0;
                //统计有资格投Lj票的分类器数量
                lengthVotes[subsetSelectLabels[i][j]]++;
            }
        }

        //组合预测结果,进行投票,投票过半的标签被并入标记结果.
        double[] finalVotevalue = new double[numLabels];	//存储标签Li的得票数和投票总数的比值
        boolean[] annoResult = new boolean[numLabels];	//存储最终的标注结果
        
        for (int i = 0; i < numLabels; i++) {
            if (lengthVotes[i] != 0) {
                finalVotevalue[i] = sumVotes[i] / lengthVotes[i];	//计算标签Li的得票数和投票总数的比值
            } else {
            	//得票数为0
                finalVotevalue[i] = 0;
            }
            
            if (finalVotevalue[i] >= threshold)	//如果得票过半数,则该标签并入标注结果.
            	annoResult[i] = true;
            else 
            	annoResult[i] = false;
        }

        //讲最终的结果存入开源库的多标签标注结果的封装类型中,因为是重写的方法,所以返回类型只能是MultiLabelOutput
        //其实返回annoResult更便于后续处理.
        MultiLabelOutput mlo = new MultiLabelOutput(annoResult, finalVotevalue);
        return mlo;
    }


	@Override
	public TechnicalInformation getTechnicalInformation() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String globalInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//计算二项式的值
    public static int binomial(int n, int m) {
        int[] b = new int[n + 1];
        b[0] = 1;
        for (int i = 1; i <= n; i++) {
            b[i] = 1;
            for (int j = i - 1; j > 0; --j) {
                b[j] += b[j - 1];
            }
        }
        return b[m];
    }
}