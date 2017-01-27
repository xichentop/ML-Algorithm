//package hw3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * implementation of Multinomial NB classifier 
 * @author xichentop
 * @version 1/24/17
 *
 */
public class NaiveBayesClassifier_Multinomial {

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		
		if (args.length!=6){
			System.err.println("error: 6 arguments needed <training_data> <test_data> <class_prior_delta> <cond_prob_delta> <model_file> <sys_output>");
			System.exit(-1);		
		}
		String training_file = args[0];
		String test_file = args[1];
		double class_prior_delta = Double.parseDouble(args[2]);
		double c_prob_delta = Double.parseDouble(args[3]);
		String model_file = args[4];
		String sys_output = args[5];
		
		/**
		 * testing
		 */
//		String training_file = "C:\\Users\\xichentop\\Documents\\ling572_2017\\hw3\\examples\\train.vectors.txt";
//		String test_file = "C:\\Users\\xichentop\\Documents\\ling572_2017\\hw3\\examples\\test.vectors.txt";
//		double class_prior_delta = 0;
//		double c_prob_delta = 0.1;
//		String model_file = "C:\\Users\\xichentop\\Documents\\ling572_2017\\hw3\\model_file";
//		String sys_output = "C:\\Users\\xichentop\\Documents\\ling572_2017\\hw3\\sys_output";
		
		/**
		 * storage
		 */
		Map<String, Integer> feat2int = new HashMap<>();  //mapping String feature to Integer
		List<String> int2feat = new ArrayList<>(); //mapping int back to String feature
		Map<String, Map<Integer, Boolean>> training_label2inst2bool = new HashMap<>(); //{label: {inst: true}}
		List<Map<Integer, Integer>> training_inst2feat2freq = new ArrayList<>(); //{inst: {feat: freq}}
		Map<String, Integer> label2count = new HashMap<>(); //mapping class label to number of instances in data
		List<String> training_inst2label = new ArrayList<>();
		/**
		 * read in training instances
		 */
		BufferedReader reader = new BufferedReader(new FileReader(training_file));
		int inst_idx = 0;
		int feat_idx = 0;
		String line;
		while ((line = reader.readLine())!=null){
			line = line.replaceAll(":", " ");
			String[] elements = line.split("\\s+");
			String label = elements[0];
			//remember the true label of the instance
			training_inst2label.add(label);
			//update label counts
			Integer ct;
			if ((ct=label2count.get(label))==null){
				label2count.put(label, 1);
			}else{
				label2count.put(label, ct+1);
			}
			
			//update training_inst2feat2freq
			training_inst2feat2freq.add(new HashMap<>());
			Map<Integer, Integer> feat2freq = training_inst2feat2freq.get(inst_idx);
			//update fea2int and int2feat 
			for (int i=1; i<elements.length; i+=2){
				String feat = elements[i];
				if (!feat2int.containsKey(feat)){
					feat2int.put(feat, feat_idx);
					int2feat.add(feat);	
					feat_idx++;//counter increment
				}
				feat2freq.put(feat2int.get(feat), Integer.parseInt(elements[i+1]));
			}
			
			
			//update label2inst2bool and inst_idx
			if (!training_label2inst2bool.containsKey(label)){
				training_label2inst2bool.put(label, new HashMap<Integer, Boolean>());
			}
			training_label2inst2bool.get(label).put(inst_idx, true);
			inst_idx++;
			
		}
		reader.close();
		
		/**
		 * calc class prior p(c_i) and conditional prob p(f|c)
		 */
		Map<String, double[]> class_prior_map = new HashMap<>(); //{label: [p, log_p]}
		Map<String, Map<Integer, double[]>> cond_prob_map = new HashMap<>(); //{label:{feat: [p, lgprob]}}
		Map<String, Map<Integer, Integer>> label2feat2count = new HashMap<>(); //{label:{feat:count of such feat with label in all instances}}
		int total_num_inst = label2count.values().stream().mapToInt(Integer::intValue).sum();
		int total_num_feat = int2feat.size();
		for (String label: label2count.keySet()){
			//add-delta smoothing for class priors
			double p = (class_prior_delta + label2count.get(label))/(label2count.size()*class_prior_delta+ total_num_inst);
			double log_p = Math.log10(p);
			class_prior_map.put(label, new double[]{p, log_p});
			
			//cond prob
			Map<Integer, Integer> feat2freq = new HashMap<>();
			int sum_label = 0; //sum of feat counts with label c_j
			Map<Integer, Boolean> inst2bool =  training_label2inst2bool.get(label);
			for (Integer inst: inst2bool.keySet()){
				for (Map.Entry<Integer, Integer> entry : training_inst2feat2freq.get(inst).entrySet()){
					if (!feat2freq.containsKey(entry.getKey())){
						feat2freq.put(entry.getKey(), entry.getValue());
					}
					else{
						feat2freq.put(entry.getKey(), feat2freq.get(entry.getKey())+entry.getValue());
					}
					sum_label+=entry.getValue();
				}
			}
			label2feat2count.put(label, feat2freq);
			
//			cond_prob_map.put(label, new HashMap<>());
			Map<Integer, double[]> feat2prob = new HashMap<>();
			for (Integer feat : feat2int.values()){
				int f_count = feat2freq.containsKey(feat)? feat2freq.get(feat):0;
				double c_p = (c_prob_delta + f_count )/(total_num_feat*c_prob_delta+sum_label);
				double lg_c_p = Math.log10(c_p);
				feat2prob.put(feat, new double[]{c_p, lg_c_p});
			}
			cond_prob_map.put(label, feat2prob);
		}
		
		/**
		 *  print model_file 
		 */
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(model_file)));
		writer.println("%%%%% prior prob P(c) %%%%%");
		class_prior_map.entrySet().stream().forEach(entry ->{
			writer.printf("%s\t%s\t%s\n", entry.getKey(),entry.getValue()[0],entry.getValue()[1]);
		});
		writer.println("%%%%% conditional prob P(f|c) %%%%%");
		for (String label: cond_prob_map.keySet()){
			writer.printf("%%%%%%%%%% conditional prob P(f|c) c=%s %%%%%%%%%%\n", label);
			cond_prob_map.get(label).entrySet().stream().forEach(entry->{
				writer.printf("%s\t%s\t%s\t%s\n", int2feat.get(entry.getKey()), label, entry.getValue()[0], entry.getValue()[1]);
			});
		}
		
		
		writer.close();
		
		
		
		/**
		 * read in testing data
		 */
		List<Map<Integer, Integer>> test_data = new ArrayList<>(); //list of instances composed of int repr of feats and their freq
		List<String> test_inst2label = new ArrayList<>(); //mapping instance idx to actual label
		reader = new BufferedReader(new FileReader(test_file));
		while ((line = reader.readLine())!=null){
			Map<Integer, Integer> instance = new HashMap<>();//{feature:freq}
			line = line.replaceAll(":", " ");
			String[] elements = line.split("\\s+");
			for (int i=1; i<elements.length; i+=2){
				String feat = elements[i];
				Integer f;
				if ((f=feat2int.get(feat))!=null){
					instance.put(f, new Integer(elements[i+1]));
				}
				//ignore feats in test instances which do not belong to the existing features of the training instances
			}
			test_data.add(instance);
			//remember true label
			test_inst2label.add(elements[0]);
		}
		reader.close();
		
		
		/**
		 * classify testing data
		 */
		List<Map<String, Double>> inst2label2prob_training = classify(training_inst2feat2freq, cond_prob_map, class_prior_map);
		List<Map<String, Double>> inst2label2prob_test=classify(test_data, cond_prob_map, class_prior_map);
		
		
		/**
		 * print sys_output
		 */
		PrintWriter sys_out = new PrintWriter(new BufferedWriter(new FileWriter(sys_output)));
		sys_out.println("%%%%% training data:");
		List<String> result_training_inst2label = get_classification_results(sys_out, inst2label2prob_training);
		sys_out.println("%%%%% test data:");
		List<String> result_test_inst2label = get_classification_results(sys_out, inst2label2prob_test);
		
		sys_out.close();
		
		
		/**
		 * print acc_file
		 */
		//map to facilitate mapping testing results in confusion matrix
		Map<String, Integer> label2int = new HashMap<>();
		int label_idx = 0;
		for (String label: label2count.keySet()){
			label2int.put(label, label_idx);
			label_idx++;
		}
		print_accuracy(result_training_inst2label, training_inst2label, label2int, "training");
		print_accuracy(result_test_inst2label, test_inst2label, label2int, "test");
		
		
		/**
		 * testing
		 */
//		System.out.println(training_label2inst2bool);
//		System.out.println(training_inst2feat2freq);
//		System.out.println(cond_prob_map);
//		System.out.println(test_data);
		
		
		long end = System.currentTimeMillis();
		System.err.println("Time: "+ (end-start) + " ms");
	}
	/**
	 * 
	 * @param result_inst2label the testing result
	 * @param inst2label the true result
	 * @param label2int matrix length and height
	 * @param string training or test
	 */
	private static void print_accuracy(List<String> result_inst2label,
			List<String> inst2label, Map<String, Integer> label2int, String train_or_test) {
		int[][] matrix = new int[label2int.size()][label2int.size()];
		
		
		for (int i=0; i<result_inst2label.size(); i++){
			//     row: orig label                   col: test result
			matrix[label2int.get(inst2label.get(i))][label2int.get(result_inst2label.get(i))]++;
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

	/**
	 * gets classification results and print to sys_output
	 * @param sys_out
	 * @param inst2label2prob
	 * @return a list of classification results for the input data
	 */
	private static List<String> get_classification_results(PrintWriter sys_out,
			List<Map<String, Double>> inst2label2prob) {
		List<String> result_inst2label = new ArrayList<>();
		for (int i=0; i<inst2label2prob.size(); i++){
			Map<String, Double> sortedMap = inst2label2prob.get(i).entrySet().stream()
			.sorted((v1, v2)-> v2.getValue().compareTo(v1.getValue()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)->e2, LinkedHashMap::new));
			
//			System.out.println(sortedMap);
			
			sys_out.print("array:"+i+" ");
			int index = 0;
			double max_lg=0;
			double sum = 0;
			double[] ps = new double[sortedMap.size()];
			for (String key: sortedMap.keySet()){
				if (index==0){
					sys_out.print(key);
					result_inst2label.add(key); //add label to classification result
					max_lg = sortedMap.get(key);
				}
				double p = Math.pow(10, sortedMap.get(key)-max_lg);
//				System.out.print("p"+index+": "+p);
//				System.out.println();
				sum += p;
//				System.out.println("sum = "+sum);
				ps[index] = p;
				index++;
//				sys_out.printf(" %s %s", key, logp2p(sortedMap.get(key).doubleValue(), max_lg));
			}
			
			index = 0;
			for (String key: sortedMap.keySet()){
				sys_out.printf(" %s %s", key, ps[index]/sum);
				index++;
			}
			sys_out.println();
		}
		return result_inst2label;
	}
	private static List<Map<String, Double>> classify(List<Map<Integer, Integer>> data, Map<String, Map<Integer, double[]>> cond_prob_map, Map<String, double[]> class_prior_map) {
		List<Map<String, Double>> inst2label2prob = new ArrayList<>();
				
		for (int i=0; i<data.size();i++){
			Map<Integer, Integer> instance = data.get(i);
			Map<String, Double> map = new HashMap<>();
			for (String label: cond_prob_map.keySet()){
				double logp_label = class_prior_map.get(label)[1];//sum the log prob for all features in this class
				double sum=0; //sum of logp*num_feat_in_inst when feature is present in instance
				for (Integer feat: instance.keySet()){//for every feature present in instance
					double[] p_arr;
					if ((p_arr=cond_prob_map.get(label).get(feat))!=null){//there's a chance such feat exist in V but not in this label class
						sum+= instance.get(feat)*p_arr[1];
					}
				}
				double logp_inst = logp_label + sum; //logp for the instance
				map.put(label, logp_inst);
			}
			inst2label2prob.add(map);
		}
		return inst2label2prob;
	}

}
