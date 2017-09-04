import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/*
 * Cette classe permet de générer un classifieur à partir de trois données que sont :
 * 	-> un fichier d'entrée (champs input)
 * 	-> un algorithme de classification (champs algo)
 * 	-> un ensemble de traits (champs featureSet)
 * Le constructeur de l'objet prend en paramètre ces trois données, puis appelle la fonction CalculModel.
 * La méthode CalculModel fait appel à l'API Weka, qui calcule le classifieur à partir des données fournies.
 * La sortie de cette fonction est un fichier .model accessible sous le dossier "models".
 * 
 * 	NB : Les ensemble de traits correspondent à des listes de nombre dont chacune correspond à un feature particulier.
 * 		 Les features seléctionnés par ces listes seront supprimées de l'ensemble lors du calcul par l'agorithme.
 * 		 Cf. fichier XXX pour le détail de l'appariement nombre / trait.
 */

public class SetModel {
	String algo;
	String featureSet;
	String input;
	String output;
	SMO smo;
	J48 tree;
	NaiveBayes nb;
	
	public SetModel (String input, String algo, String features) throws Exception {
		this.algo = algo;
		this.featureSet = features;
		this.input = input;
		this.CalculModel();
	}
	
	public void CalculModel () throws Exception{
		File f = new File("models");
		if (!f.exists()){
			f.mkdir();
		}
		String output = "models/Model";
		int[] array1 = {0,1,7,15,16,17,32,34};
		int[] array2 = {0,1,7,15,16,17,32,34,11,21};
		int[] array3 = {0,1,2,3,6,7,12,15,16,17,18,20,25,28,31,32,33,34,35,37};
		BufferedReader breader = null;
		breader = new BufferedReader(new FileReader("learningData/"+this.input));
		Instances first = new Instances(breader);
		Remove remove = new Remove();
		if (this.featureSet.equals("allFeatures")){
			output = output+"_allFeatures";
			remove.setAttributeIndicesArray(array1);
		}
		if (this.featureSet.equals("notOralFeatures")){
			output = output+"_notOralFeatures";
			remove.setAttributeIndicesArray(array2);
		}
		if (this.featureSet.equals("relationalFeatures")){
			output = output+"_relationalFeatures";
			remove.setAttributeIndicesArray(array3);
		}
		if (!this.featureSet.equals("relationalFeatures") && !this.featureSet.equals("notOralFeatures") && !this.featureSet.equals("allFeatures") ){
			System.out.println("dans le else "+this.featureSet);
			//String file = .substring(0,this.featureSet.length()-4);
			output = output+"_"+this.featureSet;
			int[] array4 = new FeatureToList().GetFeatureFromFile("models/"+this.featureSet+".txt");
			remove.setAttributeIndicesArray(array4);
		}
		remove.setInputFormat(first);
		Instances train = Filter.useFilter(first, remove);
		train.setClassIndex(train.numAttributes()-1);
		breader.close();
		
		if (this.algo.equals("SVM")){
			output = output+"_SVM.model";
			SMO model = new SMO();
			model.buildClassifier(train);
			SerializationHelper.write(output, model);
		}
		if (this.algo.equals("J48")){
			output = output+"_J48.model";
			J48 model = new J48();
			model.buildClassifier(train);
			SerializationHelper.write(output, model);
		}
		if (this.algo.equals("NaiveBayes")){
			output = output+"_NaiveBayes.model";
			NaiveBayes model = new NaiveBayes();
			model.buildClassifier(train);
			SerializationHelper.write(output, model);
		}
	}
}
