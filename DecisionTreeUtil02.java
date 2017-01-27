//package hw2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DecisionTreeUtil02 {

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		/**
		 * read in and store training/testing files
		 */
		if (args.length!=6){
			System.err.println("error: 6 args needed: training_data test_data max_depth"
					+" min_gain model_file sys_output");
			System.exit(-1);
		}
		String data_file = args[0];
		String test_file = args[1];
		int max_depth = Integer.parseInt(args[2]);
		double min_gain = Double.parseDouble(args[3]);
		String model_file = args[4];
		String sys_output = args[5];
//		int max_depth = 50;
//		double min_gain = 0.1;
//		String data_file = "C:\\Users\\xichentop\\Documents\\ling572_2017\\hw2\\train.vectors.txt";
//		String model_file = "C:\\Users\\xichentop\\Documents\\ling572_2017\\hw2\\m_output";
//		String test_file = "C:\\Users\\xichentop\\Documents\\ling572_2017\\hw2\\test.vectors.txt";
//		String sys_output = "C:\\Users\\xichentop\\Documents\\ling572_2017\\hw2\\sys_out";
		List<Map<Integer, Boolean>> data = new ArrayList<>(); //list of instances consisting of a list of int_feat
		Map<Integer, List<Integer>> feat2inst = new HashMap<>();//mapping feature to a list of instance idx that contain the feature
		List<String> inst2label = new ArrayList<>(); //mapping instance idx to class label
		Map<String, Integer> label2int = new HashMap<>(); 
		Map<String, List<Integer>> label2inst = new HashMap<>(); //mapping class label to idx of instances in data
		Map<String, Integer> feat2int = new HashMap<>();  //mapping String feature to Integer
		int idx = 0;//feature integer counter
		List<String> int2feat = new ArrayList<>(); //mapping int back to String feature
		BufferedReader reader = new BufferedReader(new FileReader(data_file));
		String line;
		while ((line = reader.readLine())!=null){
			Map<Integer, Boolean> instance = new HashMap<>();//list of features
			line = line.replaceAll(":\\d+", "");
			String[] elements = line.split("\\s+");
			for (int i=1; i<elements.length; i++){
				String feat = elements[i];
				if (!feat2int.containsKey(feat)){
					feat2int.put(feat, idx);
					int2feat.add(feat);	
					idx++;//counter increment
				}
				int feat_idx = feat2int.get(feat);
				instance.put(feat_idx, true);//add feature to instance
				if (feat2inst.get(feat_idx)==null){
					feat2inst.put(feat_idx, new ArrayList<Integer>());
				}
				feat2inst.get(feat_idx).add(data.size());//add inst idx
			}
			data.add(instance);//add instance to data
			String curr_label = elements[0];
			inst2label.add(curr_label);
			if (!label2inst.containsKey(curr_label)){
				label2inst.put(curr_label, new ArrayList<>());
			}
			label2inst.get(curr_label).add(data.size()-1); //add inst idx
//			label2inst.put(elements[1], );//map idx of instance to class label
		}
		reader.close();
		int l_idx = 0;
		for (String key: label2inst.keySet()){
			label2int.put(key, l_idx);
			l_idx++;
		}
//		System.out.println(data);
//		System.out.println(data.size());
//		System.out.println(feat2inst);
//		System.out.println(feat2inst.size());
//		System.out.println(feat2int.size());
//		System.out.println(inst2label);
//		System.out.println(inst2label.size());
		
		/**
		 * calculate entropy for the whole set of data and create Decision Tree root node
		 */
		double entropy = 0;
		for (String label : label2inst.keySet()){
			double p = label2inst.get(label).size()*1.0/data.size();//percentage of labeled instances in all instances
			entropy+= -p*Math.log(p)/Math.log(2);
		}
//		System.out.println(entropy);
		/**
		 * create decision tree
		 */
		
		DecisionTree02 dt = new DecisionTree02( max_depth, min_gain, data, int2feat);
		dt.root = dt.new Node(entropy, label2inst, data.size(), 0);
		List<Integer> features = new ArrayList<>(feat2int.values());
//		System.out.println(dt.root.getFeature());
		dt.growTree(dt.root, features);
		
		//print model
		PrintWriter model = new PrintWriter(new BufferedWriter(new FileWriter(model_file)));
		dt.printTree(dt.root, model);
		model.close();
		//decode
		//read in testing file 
		//Use bufferedReader, as alteration is done to the stream
//		Files.lines(Paths.get(test_file))
//		     
//		     .mapToInt(feat2int:get);
//		     .filter(f->f!=null)
//		     .
		
//		decode(BufferedReader, feat2int, int2feat)
		List<Map<Integer, Boolean>> test_data = new ArrayList<>();
		List<String> test_inst2label = new ArrayList<>();
		reader = new BufferedReader(new FileReader(test_file));
		while ((line = reader.readLine())!=null){
			Map<Integer, Boolean> instance = new HashMap<>();//list of features from test file
			line = line.replaceAll(":\\d+", "");
			String[] elements = line.split("\\s+");
			for (int i=1; i<elements.length; i++){
				String feat = elements[i];
				Integer f;
				if ((f=feat2int.get(feat))!=null){
					instance.put(f, true);
				}
			}
			test_data.add(instance);
			test_inst2label.add(elements[0]);
		}
		reader.close();
//		System.out.println(test_data.size());
		//get classification results on test data
		List<Map<String, Double>> test_results =  dt.classify(test_data);
//		System.out.println(results.size());
		//get classification results on training data
		List<Map<String, Double>> train_results = dt.classify(data);
		//output classification result to sys_ex
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(sys_output, true)));
		out.println("#### training results ####");
		print_results(train_results, out);
		out.println();
		out.println("#### testing results ####");
		print_results(test_results, out);
		out.close();
		
		
//		label2inst.keySet()
		
		 print_confusion_matrix(train_results, inst2label, label2int, "training");
		 print_confusion_matrix(test_results, test_inst2label, label2int, "test");
//		Set<String> labels = label2inst.keySet();
		
		
		
		long end = System.currentTimeMillis();
		System.err.println("Time = "+(end-start)*1.0/60000+"minutes");
	
	}

	private static void print_confusion_matrix(List<Map<String, Double>> results, List<String> inst2label, Map<String, Integer> label2int, String train_or_test) {
		int[][] matrix = new int[3][3];
				
		for (int i=0; i<results.size(); i++){
			String label = best_estimate(results.get(i));
			//     row: orig label                   col(): result
			matrix[label2int.get(inst2label.get(i))][label2int.get(label)]++;
		}
		System.out.println("Confusion matrix for the "+train_or_test+" data:");
		System.out.println("row is the truth, column is the system output");
		Set<String> labels = label2int.keySet();
		System.out.println("                 "+labels);
		int i = 0;
		int sum = 0;
		int correct = 0;
		for (String label: labels){
			System.out.print(label);
			for (int j=0;j<matrix[i].length;j++){
				System.out.print("\t"+matrix[i][j]);
				sum+=matrix[i][j];
				if (i==j){
					correct+=matrix[i][j];
				}
			}
			System.out.println();
			i++;
		}
		double accuracy = correct*1.0/sum;  
		System.out.println(train_or_test+" accuracy = "+accuracy);
		System.out.println();
	}

	private static String best_estimate(Map<String, Double> map) {
		double max_p = 0;
		String label = null;
		for (String key : map.keySet()){
			double p = map.get(key);
			if (p >=max_p){
				max_p = p;
				label = key;
			}
		}
		return label;
	}

	private static void print_results(List<Map<String, Double>> results, PrintWriter out) {
		for (int i=0; i<results.size();i++){
			out.print("array:"+i);
			Map<String, Double>re = results.get(i);
			for (String key: re.keySet()){
				out.printf(" %s %s", key, re.get(key));
			}
			out.println();
		}
	}
	

}
