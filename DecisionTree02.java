//package hw2;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

//import hw2.DecisionTree.Node;

public class DecisionTree02 {
	/**
	 * inner class Node
	 */
	public class Node{
		private Node left; //where feature = 1
		private Node right; //where feature = 0
		private double entropy; //entropy of the given set of data
		private int feature; //feature to split on
		private Map<String, List<Integer>> map; //mapping each label to the list of instances associated with said label
		private int num_inst; //total number of instances available
		private int depth;
//		private List<Integer> feat_vector; //feature 
//		int depth;
//		int[] data; //arr of reference idx to list of instances
		
		Node(){
			
		}
		
		Node(double entropy, Map<String, List<Integer>> map, int num_inst, int depth){
			left = null;
			right = null;
			this.entropy = entropy;
			this.feature = -1;
			this.map = map;
			this.num_inst = num_inst;
			this.depth = depth;
		}
		
		void setFeature(int feature){
			this.feature = feature;
		}
		int getFeature(){
			return feature;
		}
//		void CreateLeftChild(double entropy, int feature, ){
//				this.left = new Node();
//				this.left.entropy = c_entropy1;
//				this.left.feature = feature;
//				
//		}
//		
//		void CreateRightChild()
//		{this.right = new Node();
//		this.right.entropy = c_entropy0;
//		this.right.feature = feature;
//			
//		}
		
//		void createChildNodes(double c_entropy1, double c_entropy0, int feature){
//			this.left = new Node();
//			this.left.entropy = c_entropy1;
//			this.left.feature = feature;
//			this.right = new Node();
//			this.right.entropy = c_entropy0;
//			this.right.feature = feature;
//		}
		
		int bestSplit(List<Integer> fv){
			
			List<Map<String, List<Integer>>> split_data = null; //list of 2 sets of data split on feature
			double max_gain = min_gain;
			double max_c_entropy1 = 0;
			double max_c_entropy0 =0 ;
			int feature_idx = -1;
				
			for (int f: fv){
				Map<String, List<Integer>> f_map = new HashMap<>(); //mapping label to list of inst indices containing said feature
				Map<String, List<Integer>> nf_map = new HashMap<>(); //mapping label to list of inst indices which do not contain said feature
				
				int total_f_count =0; //total count of each feature across class labels
				int total_nf_count = 0; //total non-feature count across class labels
				List<Integer> f_counts = new ArrayList<>(); //list of feature counts for each class
				List<Integer> nf_counts = new ArrayList<>(); // list of non-feature counts for each class
				double c_entropy1 = 0;//H(S|f1=1) = Sum over {guns, mideast, misc} p(Si|f1=1)*log2(p(Si|f1=1))
				double c_entropy0 = 0; //H(S|f1=0)
				for (String label: this.map.keySet()){
					List<Integer> list1 = new ArrayList<>(); //list of data idx with feature
					List<Integer> list0 = new ArrayList<>(); //list of data idx without feature
					int f_count =0;//count of each feature =1 assoc. with a certain class label
					int nf_count = 0; //count of each feature = 0
					for (int idx: map.get(label)){
						if (data.get(idx).get(f)==null){
							nf_count++;
							list0.add(idx);
						}else{
							f_count++;
							list1.add(idx);
						}
					}
						f_counts.add(f_count);
						nf_counts.add(nf_count);
						total_f_count+=f_count;
						total_nf_count += nf_count;
						f_map.put(label, list1);
						nf_map.put(label, list0);
					}
					//calc conditional entropies
					for (int count : f_counts){
						if (count !=0 && count!=total_f_count){
							double p = count*1.0/total_f_count; //conditional prob: num(feature AND label)/total_num(feature)
							c_entropy1 += -p*(Math.log(p)/Math.log(2)); //conditional entropy of feature existent
						}
					}
					for (int count: nf_counts){
//						int total_nf_count = data.size()-total_f_count;
						if (count != 0 && count != total_nf_count){
							double p = count*1.0/total_nf_count;// num(non-feature AND label)/total_num(non-feature)
							c_entropy0 += -p*(Math.log(p)/Math.log(2));//conditional entropy of non-feature
						}
					}
					//total_count*1.0/num_inst == percentage of instances with this feature in all instances
					double info_gain = entropy - total_f_count*1.0/num_inst*c_entropy1 - total_nf_count*1.0/num_inst*c_entropy0;
//					System.out.println("info_gain for feature " + int2feat.get(i)+" is "+info_gain);
					if (info_gain>=max_gain){
						max_gain = info_gain;
//						max_entropy = entropy;
						max_c_entropy1 = c_entropy1;
						max_c_entropy0 = c_entropy0;
						feature_idx = f;
						split_data = new ArrayList<>();
						split_data.add(f_map);
						split_data.add(nf_map);
					}
					
				}
//				if (feature_idx == -1){
//					System.err.println("error: no split feature was found");
//					System.exit(-1);
//				}
				
				if (depth < max_depth && split_data !=null){ //if not reached max_depth
//					System.out.println(split_data);
					//create new child nodes to curr node
//					createChildNodes(max_c_entropy1, max_c_entropy0, feature_idx);
					int f_num = split_data.isEmpty()? 0: split_data.get(0).values().stream().mapToInt(List:: size).sum();
					int nf_num = split_data.isEmpty()? 0: split_data.get(1).values().stream().mapToInt(List:: size).sum();
					if (f_num!= 0){
						this.left = new Node(max_c_entropy1, split_data.get(0), f_num, this.depth+1);
						System.err.println("a new node has been created with feature=1: "+feature_idx);
					}
					if (nf_num!=0){
						this.right = new Node(max_c_entropy0, split_data.get(1), nf_num, this.depth+1);
						System.err.println("a new node has been created with feature=0: "+feature_idx);
						
					}
				}
				
				
			
			return feature_idx;
		}

		public boolean isLeaf() {
			return left == null && right == null;
		}
	}
	
	/**
	 * fields
	 */
	Node root;
	int max_depth;
	double min_gain;
	List<Map<Integer, Boolean>> data; //the whole set of data
	List<String> int2feat; 
	
	
	public DecisionTree02(){
	}
	
	
	public DecisionTree02( int max_depth, double min_gain, List<Map<Integer, Boolean>> data, List<String> int2feat){
//		this.root = root;
		this.max_depth = max_depth;
		this.min_gain = min_gain;
		this.data = data;
		this.int2feat = int2feat;
	}
	
	void growTree(Node node, List<Integer> fv){
		//precondition check
		// exceeding max-depth, empty feature vector, or all instances of one class or number of instances==0
		if (node==null|| node.depth>=max_depth||fv.isEmpty()||node.entropy==0||node.num_inst ==0){
			return;
		}
		int split_feature = node.bestSplit(fv);
		if (split_feature!= -1){
			node.setFeature(split_feature);
			List<Integer> new_fv =  fv.stream().filter(f->f!=node.feature).collect(Collectors.toList());
			growTree(node.left, new_fv);
			growTree(node.right, new_fv);
		}
		
		
	}
	
	void printTree(Node node, PrintWriter model){
		Queue<Node> path = new LinkedList<>();
		printTree(node, path, model, false);
		model.close();
		
	}


	private void printTree(Node node, Queue<Node> path, PrintWriter model, boolean bool) {
		if (node == null){
			return;
		}
		path.add(node);
		if (node.isLeaf()&&node.num_inst!=0){ //when the leaf node contains any instance
			while (path.size()>2){
				if (bool){
					model.print(int2feat.get(path.poll().feature)+"&");
				}else{
					model.print("!"+int2feat.get(path.poll().feature)+"&");
				}
			}
			try {
				if (bool){
					model.print(int2feat.get(path.poll().feature)+" ");
				}else{
					model.print("!"+int2feat.get(path.poll().feature)+" ");
			        }	
			}catch (Exception e){
				System.err.println("error: no feature split can be made with the min_gain set to "+this.min_gain);
				System.exit(-1);
			}
			Node leafNode = path.poll();
			model.print(leafNode.num_inst);
			for (String label: leafNode.map.keySet()){
				   model.print(" "+label);
				   model.print(" "+leafNode.map.get(label).size()*1.0/leafNode.num_inst);
			   }
			model.println();
			return;
		}else{
		    printTree(node.left, new LinkedList<>(path), model, true);
		    printTree(node.right, new LinkedList<>(path), model, false);
		}
	}
	
	List<Map<String, Double>> classify(List<Map<Integer, Boolean>> instances){
		List<Map<String, Double>> list = new ArrayList<>();
		for (Map<Integer,Boolean> inst : instances){
			Map<String, Double> result = predict(this.root, inst);
			if (!result.isEmpty()){
				list.add(result);
			}
		}
		return list;
	}
	Map<String, Double> predict(Node node, Map<Integer, Boolean> inst){
		Map<String, Double> result = new HashMap<>();
		if (node==null){
			return result;
		}
		
		if (inst.get(node.feature)!= null){
			result = predict(node.left, inst);
		}else{
			result = predict(node.right, inst);
		}
		if (node.isLeaf()&&node.num_inst!=0){
//			sys_ex.print("array:"+idx);
			for (String label: node.map.keySet()){
				  result.put(label, node.map.get(label).size()*1.0/node.num_inst);
			  }
		}
		return result;
		
	}
	
	

}
