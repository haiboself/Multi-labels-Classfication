import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

import haibo.alogrithm.Util;
import mulan.classifier.MultiLabelOutput;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.MultiLabelInstances;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

public class MuLanExp2 {

    public static void main(String[] args) throws Exception {
        String arffFilename = "./data/test/birds-train.arff";
        String xmlFilename = "./data/test/birds.xml";
        

        MultiLabelInstances dataset = new MultiLabelInstances(arffFilename, xmlFilename);

        RAkEL model = new RAkEL(new LabelPowerset(new J48()));

        model.build(dataset);

        //ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("./data/test/res")));
        //out.writeObject(model);
    	
    	/*ObjectInputStream input = new ObjectInputStream(new FileInputStream("./data/test/res"));
    	RAkEL model = (RAkEL) input.readObject();*/

        String unlabeledFilename = "./data/test/birds-test.arff" ;
        FileReader reader = new FileReader(unlabeledFilename);
        Instances unlabeledData = new Instances(reader);

        int numInstances = unlabeledData.numInstances();

        for (int instanceIndex = 0; instanceIndex < 1; instanceIndex++) {
            Instance instance = unlabeledData.instance(instanceIndex);
            MultiLabelOutput output = model.makePrediction(instance);
            // do necessary operations with provided prediction output, here just print it out
            
            boolean res[] = output.getBipartition();
            for(int i=0;i<res.length;i++){
            	if(res[i]) System.out.print(i+"  ");
            }
            System.out.println();
            System.out.println(output);
        }
    }
}