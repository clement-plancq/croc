import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;


public class ResolverSystem {
	private String dirName;
	private String fileName = new String();
	private Classifier cls;
	private Instances test;
	private Instances labeled;
	private double blanc;
	private double muc;
	private double bcub;
	private double ceafm;
	private double ceafe;
	
	public ResolverSystem(String dir, String file, String model) throws Exception{
		this.dirName = dir;
		fileName = file;
		if(file.contains("/")){
			fileName = file.substring(file.lastIndexOf("/")+1,file.length());
		}
		this.cls = (Classifier)SerializationHelper.read(model);
		this.UseModel(file, model);
		this.OutputFile(file, model.substring(7,model.length()));
	}
	
	public double GetMuc(){
		return this.muc;
	}
	public double GetBlanc(){
		return this.blanc;
	}
	public double GetBcub(){
		return this.bcub;
	}
	public double GetCeafm(){
		return this.ceafm;
	}
	public double GetCeafe(){
		return this.ceafe;
	}
	
	public void UseModel(String file, String model) throws Exception{
		BufferedReader breader = null;
		breader = new BufferedReader(new FileReader(file));
		Instances first = new Instances(breader);
		Remove remove = new Remove();
		
		if (model.contains("allFeatures")){
			int[] array= {0,1,7,15,16,17,32,34};
			remove.setAttributeIndicesArray(array);
			remove.setInputFormat(first);
		}
		if (model.contains("relationalFeatures")){
			int[] array = {0,1,2,3,6,7,12,15,16,17,18,20,25,28,31,32,33,34,35,37};
			remove.setAttributeIndicesArray(array);
			remove.setInputFormat(first);
		}
		if (model.contains("notOralFeatures")){
				int[] array = {0,1,7,15,16,17,32,34,11,21};
				remove.setAttributeIndicesArray(array);
				remove.setInputFormat(first);
		}
		if (!model.contains("notOralFeatures") && !model.contains("relationalFeatures") && !model.contains("allFeatures")){
			int firstUnderscore = model.indexOf("_");
			int lastUnderscore = model.lastIndexOf("_");
			String featureSet = model.substring(firstUnderscore+1, lastUnderscore);
			int[] array = new FeatureToList().GetFeatureFromFile("models/"+featureSet+".txt");
			remove.setAttributeIndicesArray(array);
			remove.setInputFormat(first);
		}
		
		test = Filter.useFilter(first, remove);
		test.setClassIndex(test.numAttributes()-1);
		breader.close();
		
		labeled = new Instances(test);
		for (int i=0; i<test.numInstances(); i++){
			double clsLabel = cls.classifyInstance(test.instance(i));
			labeled.instance(i).setClassValue(clsLabel);
		}
		breader.close();
	}
	
	public void OutputFile(String file, String model) throws Exception{
		BufferedWriter writer = new BufferedWriter(new FileWriter(dirName+"/"+model+"_systemReponse_"+fileName));
		writer.write(labeled.toString());
		writer.close();
	}
	
	
	public void EvalOutput(String trueFile, String systemFile, String model) throws Exception{
		BufferedWriter writer = new BufferedWriter(new FileWriter(dirName+"/resultEval_"+fileName.substring(0,fileName.length()-4)+"txt", true));
		writer.write(model+"\n");
		this.blanc = this.BLANC();
		writer.write("BLANC "+this.BLANC()+"\n");
		this.muc = Double.parseDouble(this.PerlEval("muc", trueFile, systemFile));
		writer.write("MUC "+this.PerlEval("muc", trueFile, systemFile)+"\n");
		this.bcub = Double.parseDouble(this.PerlEval("bcub", trueFile, systemFile));
		writer.write("B3 "+this.PerlEval("bcub", trueFile, systemFile)+"\n");
		this.ceafm = Double.parseDouble(this.PerlEval("ceafm", trueFile, systemFile));
		writer.write("CEAFm "+this.PerlEval("ceafm", trueFile, systemFile)+"\n");
		this.ceafe = Double.parseDouble(this.PerlEval("ceafe", trueFile, systemFile));
		writer.write("CEAFe "+this.PerlEval("ceafe", trueFile, systemFile)+"\n\n");
		writer.close();
	}
	
	public String PerlEval(String metric, String trueFile, String systemFile) throws Exception{
		Process p = Runtime.getRuntime().exec("perl lib/reference-coreference-scorers/scorer.pl "+metric+" "+trueFile+" "+systemFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;
		List<String> resu = new ArrayList<String>();
		while ((line = br.readLine()) != null){
			resu.add(line);
		}
		String last = resu.get(resu.size()-2);
		StringTokenizer st = new StringTokenizer(last);
		List<String> exemple = new ArrayList<String>();
		while (st.hasMoreTokens()){
			exemple.add(st.nextToken());
		}
		String resultat = exemple.get(exemple.size()-1);
		int index = resultat.indexOf("%");
		return resultat.substring(0, index);
	}
	
	public double BLANC() throws Exception{
		Evaluation eval = new Evaluation(test);
		eval.evaluateModel(cls, test);
		double blanc = (eval.fMeasure(0)+eval.fMeasure(1))/2;
		return blanc;
	}
}
