import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/*
 * Cette classe compare les résultats du système aux résultats de référence
 * Son accesseur demande 3 paramètres que sont :
 * 	-> Le nom du dossier où se trouve le fichier contenant les résultats du système
 * 	-> Le nom du fichier lui-même
 * 	-> Le nom du modèle de classification ayant produit les résultats
 * 
 * La méthode GetModelLabel lit le fichier .arff contenant les étiquettes produites
 * par le modèle de classification, et récupère, pour chaque instance, la valeur de sa
 * classe (ie. le dernier élément de chaque ligne).
 * Les résultats de cette extraction sont envoyés vers l'attribut label, qui est une liste
 * de la forme [COREF, COREF, NOT_COREF, COREF, NOT_COREF, NOT_COREF, ...] 
 * 
 * La méthode ClassList lit le fichier .arff puis construit deux attributs :
 * 	1) Il alimente un tableau associatif indiquant pour chaque paire de mention,
 * 	   si elle est coréférente ou nom. Ce tableau correspond à l'attribut results, 
 * 	   et est de la forme [ [1,2]:COREF, [2,3]: COREF, ..., [4,22]: NOT_COREF, ...]
 *  2) Il extrait les vraies chaînes de coréférence du fichier, et les envoie vers un ensemble
 *     de valeurs de la forme {{1,2,8,9}, {3,5,7}, {4}, {6}}.
 *     Ces vraies chaînes sont sous la forme de commentaires dans le fichier .arff, entre les
 *     mots-clés BEGIN et END
 *
 *	La méthode SystemRepinse reconstruit les chaînes de coréférence par transitivité :
 *	A partir de l'attribut results, une fonction récursive (RecurseTrans)rassemble sous
 *	une même liste les sous-listes partageant un élément 
 *  	-> ex : Si [1,2]:COREF et [2,3]: COREF, Alors on génère la chaîne [1,2,3]
 *  	L'opération se répère jusqu'à ce que toutes les unités n'apparaissent que dans une
 *  	seule liste (ie. une sele chaîne)
 *  NB : Lorsque le classifieur considère qu'une même unité a plusieurs antécédent, un filtre permet
 *       de ne conserver que celui dont elle est le plus proche (méthode RecurseFilter).
 *      -> ex : Si [12,15] = COREF et [4,15] = COREF, On ne conserve que la paire [12,15]
 *  Les résultats extraits sont stockés sous la même forme que les résultas de référence, i.e un
 *  ensemble d'ensemble t.q {{1,2}, {3,5,7,8}, {4,9}, {6}} (attribut systemReponse).
 * 
 * 	La méthode OutputWriter appelle la classe Format Transformation pour créer 2 fichiers (référence et système)
 *  au format SemEval, nécessaire à l'évaluation de la sortie automatique.
 * 
 */

public class ChainComparator {
	private File arffFile;
	private String fileName = new String();
	private String systemFile = new String();
	private String dirName = new String();
	private String modelName = new String();
	private List<String> label = new ArrayList<String>();
	private Hashtable<List<Integer>, String> results = new  Hashtable<List<Integer>, String>();
	private Set<Set<Integer>> systemReponse = new HashSet<Set<Integer>>();
	private Set<Set<Integer>> trueReponse = new HashSet<Set<Integer>>();
	private Set<Integer> listUnit = new HashSet<Integer>();
	
	public ChainComparator (String dir, String file, String model) throws Exception{
		this.dirName = dir;
		this.arffFile = new File(file);
		fileName = this.arffFile.getName();
		systemFile = model+"_systemReponse_"+fileName;
		modelName = model;
		if(file.contains("/")){
			fileName = file.substring(file.lastIndexOf("/")+1,file.length());
		}
		this.GetModelLabel();
		this.SystemReponse();
		this.OutputWriter();
	}
	
	public Set<Set<Integer>> GetSystemReponse(){
		return this.systemReponse;
	}
	
	public Set<Set<Integer>> GetTrueReponse(){
		return this.trueReponse;
	}
	
	public Set<Set<Integer>> LisToSet(List<List<Integer>> list){
		Set<Set<Integer>> out = new HashSet<Set<Integer>>();
		for (List<Integer> subList : list){
			if (!subList.isEmpty()){
				Set<Integer> ensemble = new HashSet<Integer>();
				for (Integer nb : subList){
					ensemble.add(nb);
				}
				out.add(ensemble);
			}
		}
		return out;
	}
	
	public void GetModelLabel(){
		File modelLabel = new File(dirName+"/"+systemFile);
		BufferedReader lecteur= null;
		List<String> lignes = new ArrayList<String>();
		List<List<String>> instances = new ArrayList<List<String>>();
		try{
			lecteur = new  BufferedReader(new FileReader(modelLabel));
			String ligne = null;
			try{
				while ((ligne = lecteur.readLine()) != null){
					lignes.add(ligne);
				}
			}
			catch (java.io.IOException e){}
			
			int data = lignes.indexOf("@data");
			List<String> listeTemp = lignes.subList(data+1,lignes.size());
			for (String i : listeTemp){
				i = i.replace(","," ");
				List<String> exemple = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(i);
				while (st.hasMoreTokens()){
					exemple.add(st.nextToken());
				}
			instances.add(exemple);
			}
			for (List<String> liste : instances){
				if (!liste.isEmpty()){
					String classe = liste.get(liste.size()-1);
					this.label.add(classe);
				}
			}
		}
		catch (java.io.IOException e){}
	}
	
	public void ClassList(){
		BufferedReader lecteur= null;
		List<String> lignes = new ArrayList<String>();
		try{
			lecteur = new  BufferedReader(new FileReader(this.arffFile));
			String ligne = null;
			try{
				while ((ligne = lecteur.readLine()) != null){
					lignes.add(ligne);
				}
			}
			catch (java.io.IOException e){}
		}
		catch (java.io.IOException e){}
		List<List<String>> instances = new ArrayList<List<String>>();
		int data = 0;
		if (lignes.contains("@data")){
			data = lignes.indexOf("@data");
		}
		if (lignes.contains("@DATA")){
			data = lignes.indexOf("@DATA");
		}
		List<String> listeTemp = lignes.subList(data+1,lignes.size());
		for (String i : listeTemp){
			i = i.replace(","," ");
			List<String> exemple = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(i);
			while (st.hasMoreTokens()){
				exemple.add(st.nextToken());
			}
			instances.add(exemple);
		}
		int begin = 0;
		int end = 0;
		for (List<String> liste : instances){
			if (!liste.isEmpty()){
				if (!liste.get(0).equals("%")){
					int reprise = Integer.parseInt(liste.get(liste.size()-1));
					int antecedent = Integer.parseInt(liste.get(liste.size()-2));
					listUnit.add(reprise); listUnit.add(antecedent);
					List<Integer> paire = new ArrayList<Integer>();paire.add(antecedent); paire.add(reprise);
					String classe = this.label.get(instances.indexOf(liste));
					this.results.put(paire, classe);
				}
				else{
					if(liste.contains("BEGIN")){
						begin = instances.indexOf(liste);
					}
					if(liste.contains("END")){
						end = instances.indexOf(liste);
					}
				}
			}
		}
		List<List<Integer>> TrueList = new ArrayList<List<Integer>>();
		for (List<String> subList : instances.subList(begin+1, end)){
			List<Integer> temp = new ArrayList<Integer>();
			for (String i : subList){
				if(!i.equals("%")){
					temp.add(Integer.parseInt(i));
				}
			}
			TrueList.add(temp);
		}
		for (List<Integer> reponse : TrueList){
			TrueList = RecurseTrans(reponse, TrueList);
		}
		trueReponse = AddSingletons(LisToSet(TrueList));
	}
	
	public Set<Set<Integer>> AddSingletons(Set<Set<Integer>> set){
		List<Integer> all = new ArrayList<Integer>();
		Set<Integer> singletons = new HashSet<Integer>();
		for (Set<Integer> ensemble : set){
			for (Integer i :ensemble){
				all.add(i);
			}
		}
		for (Integer i : listUnit){
			if (!all.contains(i)){
				singletons.add(i);
			}
		}
	
		for (Integer i : singletons){
				HashSet<Integer> ajouter = new HashSet<Integer>();
				ajouter.add(i);
				set.add(ajouter);
			}
		return set;
	}
	
	
	public void SystemReponse(){
		this.ClassList();
		List<List<Integer>> systemResult = new ArrayList<List<Integer>>();
		Iterator<String> itValue = this.results.values().iterator();
		Iterator<List<Integer>> itKey = this.results.keySet().iterator();
		while (itKey.hasNext()){
			List<Integer> paire = (List<Integer>)itKey.next();
			String value = (String)itValue.next();
			if (value.equals("COREF")){
				systemResult.add(paire);
			}
		}
		int verif = 0;
		while(verif<systemResult.size()){
			systemResult = RecurseFilter(systemResult.get(verif), systemResult);
			verif++;
		}
		for (List<Integer> reponse : systemResult){
			systemResult = RecurseTrans(reponse, systemResult);
		}
		systemReponse = AddSingletons(LisToSet(systemResult));
	}
	
	public List<List<Integer>> RecurseFilter(List<Integer> courant, List<List<Integer>> listPair){
		int j = 0;
		while(j< listPair.size()){
			List<Integer> liste = listPair.get(j);
			if (courant.get(1) == liste.get(1) && !courant.equals(liste)){
				if(courant.get(0)>liste.get(0)){
					listPair.remove(liste);
					RecurseFilter(courant, listPair);
				}
				else{
					listPair.remove(courant);
					RecurseFilter(liste, listPair);
				}
				j=listPair.size();
			}
			else{
				j++;
			}	
		}
		return listPair;
	}
	
	public List<List<Integer>> RecurseTrans(List<Integer> courant, List<List<Integer>> listReponse){
		List<Integer> temp = new ArrayList<Integer>();
		for (List<Integer> liste : listReponse){
			int j = 0;
			while (j<liste.size()){
				int i = liste.get(j);
				if (courant.contains(i) && !courant.equals(liste)){
					for (Integer num : liste){
						temp.add(num);
					}
					for (Integer num : courant){
						temp.add(num);
					}
					List<Integer> emptyList = new ArrayList<Integer>();
					listReponse.set(listReponse.indexOf(courant), temp);
					listReponse.set(listReponse.indexOf(liste), emptyList);
					RecurseTrans(temp,listReponse);
					j=liste.size();
				}
				else{
					j++;
				}
			}
		}
		return listReponse;
	}
	
	public void OutputWriter() throws Exception{
		//-------------------- Ecriture des fichiers qui serviront aux calculs de métriques par le programme PERL --------------------
		FormatTransformation transfo = new FormatTransformation();
		transfo.SemEvalOutput(this.dirName, this.arffFile, systemReponse, "system", modelName);
		if (!new File(dirName+"/reference_"+fileName.substring(0,fileName.length()-4)+"txt").exists()){
			transfo.SemEvalOutput(this.dirName, this.arffFile, trueReponse, "true", modelName);
		}
	}
}


