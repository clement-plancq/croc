import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathFactory;
@SuppressWarnings("unchecked")

public class PairSelection {
	private String learningDir;
	private String testDir;
	private String testFile;
	private String mode;
	private List<Integer> totalMentions = new ArrayList<Integer>();
	private Hashtable<String,List<String>> chainListId = new Hashtable<String,List<String>>();
	private List<List<Integer>> chainListNum = new ArrayList<List<Integer>>();
	private List<List<String>> metadata = new ArrayList<List<String>>();
	private List<Hashtable<String, String>> instances = new ArrayList<Hashtable<String,String>>();
	private List<List<Integer>> listeIdPaires = new ArrayList<List<Integer>>();
	
	public PairSelection (String mode, String param, String file) throws IOException{
		if (mode.equals("learning")){
			this.mode="learning";
			this.learningDir = file;
			DirectoryReader testParcoursBis = new DirectoryReader();
			for (File j : testParcoursBis.listFiles(this.learningDir)){
				if (!j.getName().contains("arff")){
					this.LearningData(this.learningDir+"/"+j.getName(),param);
				}
			}
			this.OutputArff(this.learningDir+"/learningData.arff");
		}
		if(mode.equals("test")){
			this.mode = "test";
			this.testDir = file.substring(0,file.indexOf("/"));
			this.testFile = file.substring(file.indexOf("/")+1, file.length());
			this.LearningData(this.testDir+"/"+this.testFile, param);
			this.OutputArff("ResultFiles_"+this.testFile.substring(0,this.testFile.length()-4)+"/"+this.testFile.substring(0, this.testFile.length()-3)+"arff");
		}
	}
	
	public List<List<Integer>>  GetNumAttribute(Document document){
		List<List<Integer>> sortie = new ArrayList<List<Integer>>();
		Iterator<List<String>> itValue = this.chainListId.values().iterator(); 
		while(itValue.hasNext()){
			List<String> paireId = (List<String>)itValue.next();
			List<Integer> paireNum = new ArrayList<Integer>();
			for (String unit : paireId){
				Element anchor = (Element)(XPathFactory.instance().compile("//anchor[@id='"+unit+"']").evaluateFirst(document));
				if (anchor!=null){
					int num = Integer.parseInt(anchor.getAttributeValue("num")); 
					paireNum.add(num);
				}
			}
			if(!paireNum.isEmpty()){
				sortie.add(paireNum);
			}
		}
		return sortie;
	}
	
	public List<Integer> SearchSingletons (){
		List<Integer> sortie =  new ArrayList<Integer>();
		List<Integer> inChain = new ArrayList<Integer>();
		for (List<Integer> i : this.chainListNum){
			for (Integer j : i){
				inChain.add(j);
			}
		}
		for(Integer k : this.totalMentions){
			if (!inChain.contains(k)){
				sortie.add(k);
			}
		}
		return sortie;
	}
	
	public void LearningData (String fileName, String param) throws IOException{
		//File j = new File(fileName);
			//System.out.println(j.getName());
			//String fileName = this.learningDir+"/"+j.getName();
			SAXBuilder sxb = new SAXBuilder();
			this.chainListId.clear();
			this.chainListNum.clear();
			this.totalMentions.clear();
			try{
				Document document = sxb.build(new File(fileName));
				List<Element> liste = (List<Element>)(Object)(XPathFactory.instance().compile("//anchor")).evaluate(document);
				for (int compteur = 0; compteur < liste.size(); compteur++){
					if (liste.get(compteur).getAttributeValue("num").equals(Integer.toString(compteur+1))){
						this.totalMentions.add(Integer.parseInt(liste.get(compteur).getAttributeValue("num")));
					}
				}
				List<Element> listRelations = document.getRootElement().getChild("annotations").getChildren("relation");
				if (!listRelations.isEmpty()){
					Element courant = null;
					Iterator<Element> i = listRelations.iterator();
					while (i.hasNext()){
						courant = (Element)i.next();
						List<String> listeValeurs = new ArrayList<String>();
						List<Element> paireMentions = courant.getChild("positioning").getChildren("term");
						listeValeurs.add(paireMentions.get(1).getAttributeValue("id"));
						if (this.chainListId.containsKey(paireMentions.get(1).getAttributeValue("id"))){
							listeValeurs = this.chainListId.get(paireMentions.get(1).getAttributeValue("id"));
							listeValeurs.add(paireMentions.get(0).getAttributeValue("id"));
							this.chainListId.put(paireMentions.get(1).getAttributeValue("id"), listeValeurs);
						}
						else{
							listeValeurs.add(paireMentions.get(0).getAttributeValue("id"));
							this.chainListId.put(paireMentions.get(1).getAttributeValue("id"), listeValeurs);
						}
					}
					this.chainListNum = this.GetNumAttribute(document);
					List<Integer> singletons = this.SearchSingletons();
					this.SetLearningInstances(document, fileName, param);
					for (Integer s : singletons){
						this.SetInstancesForSingletons(document, fileName, s, param);
						List<Integer> sing = new ArrayList<Integer>();
						sing.add(s);
						this.chainListNum.add(sing);
					}
				}
				/*else{
					//System.out.println(this.totalMe
				}*/
				List<Integer> singletons = this.SearchSingletons();
				for (Integer s : singletons){
					this.SetInstancesForSingletons(document, fileName, s, param);
					List<Integer> sing = new ArrayList<Integer>();
					sing.add(s);
					this.chainListNum.add(sing);
				}
			}
			catch (Exception e){System.out.println(e);}
	}
	
	public void SetInstancesForSingletons (Document document, String file, Integer i, String param) throws IOException{
			if(param.equals("small")||param.equals("medium")||param.equals("big")){
				List<Integer> subList = this.totalMentions.subList(0, this.totalMentions.indexOf(i));
				Random myRandomizer = new Random();
				if (subList.size()>=1){
					Integer x = subList.get(myRandomizer.nextInt(subList.size()));
					this.FeatureSearch(document, file, Integer.toString(x), Integer.toString(i), "NO");
					if ((param.equals("medium")||param.equals("big")) && subList.size()>1){
						Integer y = subList.get(myRandomizer.nextInt(subList.size()));
						while(y.equals(x)){
							y = subList.get(myRandomizer.nextInt(subList.size()));
						}
						this.FeatureSearch(document, file, Integer.toString(y), Integer.toString(i), "NO");
						if (param.equals("big") && subList.size()>2){
							Integer z = subList.get(myRandomizer.nextInt(subList.size()));
							while(z.equals(y) || z.equals(x)){
								z = subList.get(myRandomizer.nextInt(subList.size()));
							}
							this.FeatureSearch(document, file, Integer.toString(z), Integer.toString(i), "NO");
						}
					}
				}
			}
			if(param.equals("w20")){
				System.out.println(i);
				int m_indice = this.totalMentions.indexOf(i);
				List<Integer> precedentsMention = this.totalMentions.subList(0, m_indice);
				List<Integer> vingtPrecedents = new ArrayList<Integer>();
				int test = 20;
				while(test>0){
					if(precedentsMention.size()>=test){
							vingtPrecedents.add(precedentsMention.get(precedentsMention.size()-test));
					}
					test--;
				}
	            for (Integer PreviousMention : vingtPrecedents){
	                List<Integer> negatifs = new ArrayList<Integer>();
	                negatifs.add(PreviousMention);negatifs.add(i);
	                if (!listeIdPaires.contains(negatifs)){
	                	this.FeatureSearch(document, file, Integer.toString(PreviousMention), Integer.toString(i), "NO");
	                	this.listeIdPaires.add(negatifs);
	                }
	            }
			}
	}
	
	public void SetLearningInstances (Document document, String file, String param) throws IOException{
		for (List<Integer> i : this.chainListNum){
			Collections.sort(i);
			for (int compteur = 1; compteur<i.size(); compteur++){
				int m1_indice = this.totalMentions.indexOf(i.get(compteur-1));
				int m2_indice = this.totalMentions.indexOf(i.get(compteur));
				if(param.equals("small")||param.equals("medium")||param.equals("big")){
					List<Integer> positif = new ArrayList<Integer>();
					this.FeatureSearch(document, file, Integer.toString(i.get(compteur-1)), Integer.toString(i.get(compteur)), "YES");
					positif.add(i.get(compteur-1)); positif.add(i.get(compteur));
					this.listeIdPaires.add(positif);
					 //-------------------- Sélection de deux mentions x et y sélectionnées aléatoirement entre m1 et m2 ------------------
					List<Integer> subList = this.totalMentions.subList(m1_indice+1, m2_indice);
					Random myRandomizer = new Random();
					if (subList.size()>=1){
						Integer x = subList.get(myRandomizer.nextInt(subList.size()));
						this.FeatureSearch(document, file, Integer.toString(x), Integer.toString(i.get(compteur)), "NO");
						if ((param.equals("medium")||param.equals("big")) && subList.size()>1){
							Integer y = subList.get(myRandomizer.nextInt(subList.size()));
							while(y.equals(x)){
								y = subList.get(myRandomizer.nextInt(subList.size()));
							}
							this.FeatureSearch(document, file, Integer.toString(y), Integer.toString(i.get(compteur)), "NO");
							if (param.equals("big") && subList.size()>2){
								Integer z = subList.get(myRandomizer.nextInt(subList.size()));
								while(z.equals(y) || z.equals(x)){
									z = subList.get(myRandomizer.nextInt(subList.size()));
								}
								this.FeatureSearch(document, file, Integer.toString(z), Integer.toString(i.get(compteur)), "NO");
							}
						}
					}
				}
				if (param.equals("w20")){
					List<Integer> positif = new ArrayList<Integer>();
					this.FeatureSearch(document, file, Integer.toString(i.get(compteur-1)), Integer.toString(i.get(compteur)), "YES");
					positif.add(i.get(compteur-1)); positif.add(i.get(compteur));
					this.listeIdPaires.add(positif);
					this.SetInstancesForSingletons(document, file, i.get(compteur), param);
				}
			}
		}
	}
	
	public void FeatureSearch (Document document,String file, String num1, String num2, String classe) throws IOException{
		this.CleanContent(document);
		List<String> temp = new ArrayList<String>();
		Hashtable<String, String> featureSet = new Hashtable<String,String>();
		Element antAnchor = (Element)XPathFactory.instance().compile("//anchor[@num='" +num1+"']").evaluateFirst(document);
		Element repAnchor = (Element)XPathFactory.instance().compile("//anchor[@num='" +num2+"']").evaluateFirst(document);
		String str1 = antAnchor.getAttributeValue("id");
		String str2 = repAnchor.getAttributeValue("id");
		//featureSet.put("antecedent",antAnchor.getAttributeValue("num"));
		//featureSet.put("reprise",repAnchor.getAttributeValue("num"));
		temp.add(antAnchor.getAttributeValue("num"));temp.add(repAnchor.getAttributeValue("num"));
		//-------------------- Récupération des formes des deux mentions --------------------
		String forme1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='CONTENT']//text()").evaluateFirst(document)).getTextNormalize();
		String forme2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='CONTENT']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_FORM", forme1);
		featureSet.put("M2_FORM", forme2);
		//System.out.println(forme1+" "+forme2);
		//-------------------- Vérification de l'identité  des formes des deux mentions --------------------
		if (forme1.equals(forme2)){
			featureSet.put("ID_FORM", "YES");
		}
		else{
			featureSet.put("ID_FORM", "NO");
		}
		//-------------------- Vérification de l'identité entre sous-chaînes des deux mentions --------------------
		//Première version simple qui se contente de vérifier si une des deux mentions est sou-chaîne de l'autre
			 if (forme1.contains(forme2) || forme2.contains(forme1)){
				featureSet.put("ID_SUBFORM", "YES");
			}
			else{
				featureSet.put("ID_SUBFORM", "NO");
			}
		// Deuxième solution plus complexe qui calcule le pourcentage de mots communs entre les deux mentions:
			// premier resultat : rapport de l'intersection sur l'union (variable common_slot)
			// deuxième résultat : taux de recouvremenbt de la plus courte mention par rapport à la plus longue (variable incl_slot)
				// exemple : m1 = "un plan de ville"; m2 = "la ville"
				// common_slot = 1/5 ; incl_slot = 1/2
		//int max = 0;
		int min = 0;
		int common = 0;
		List<String> formes_m1 = new ArrayList<String>();
		List<String> formes_m2 = new ArrayList<String>();
		StringTokenizer st1 = new StringTokenizer(forme1);
		StringTokenizer st2 = new StringTokenizer(forme2);
		while (st2.hasMoreTokens()){
			formes_m2.add(st2.nextToken());
		}
		while (st1.hasMoreTokens()){
			formes_m1.add(st1.nextToken());
		}
		if (forme1.length()>forme2.length()){
			//max = formes_m1.size();
			min = formes_m2.size();
			for (String tok : formes_m2){
				if (formes_m1.contains(tok)){
					common++;
				}
			}
		}
		else{
			//max = formes_m2.size();
			min = formes_m1.size();
			for (String tok : formes_m1){
				if (formes_m2.contains(tok)){
					common++;
				}
			}
		}
		int denom = formes_m1.size()+formes_m2.size()-common;
		float common_rate = (float)common/denom;
		float incl_rate = (float)common/min;
		featureSet.put("COM_RATE", Float.toString(common_rate));
		featureSet.put("INCL_RATE", Float.toString(incl_rate));
		
		//-------------------- Vérification de l'identité des têtes lexicales des deux mentions --------------------
		String type1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//type//text()").evaluateFirst(document)).getTextNormalize();
		String type2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//type//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_TYPE", type1);
		featureSet.put("M2_TYPE", type2);
		if (type1.equals(type2) && !type1.equals("NULL")  && !type1.equals("UNK")){
			featureSet.put("ID_TYPE", "YES");
		}
		else{
			featureSet.put("ID_TYPE", "NO");
		}
		
		//-------------------- Récupération de l'identificant du speaker des deux mentions --------------------
		String speaker1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='SPEAKER']//text()").evaluateFirst(document)).getTextNormalize();
		String speaker2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='SPEAKER']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_SPK", speaker1);
		featureSet.put("M2_SPK", speaker2);
		//-------------------- Vérification de l'identité des locuteurs des deux mentions --------------------
		if (speaker1.equals(speaker2)){
			featureSet.put("ID_SPK", "YES");
		}
		else{
			featureSet.put("ID_SPK", "NO");
		}
		
		//-------------------- Récupération du type d'entités nommées des deux mentions --------------------
		String en1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='EN']//text()").evaluateFirst(document)).getTextNormalize();
		String en2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='EN']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_EN", en1);
		featureSet.put("M2_EN", en2);
		//-------------------- Vérification de l'identité des types d'entités des deux mentions --------------------
		if (en1.equals(en2)){
			if (!en1.equals("NULL") && !en1.equals("UNK") && !en2.equals("NULL") && !en2.equals("UNK")
					&& !en1.equals("NO") && !en2.equals("NO")){
				featureSet.put("ID_EN", "YES");
			}
			else{
				featureSet.put("ID_EN", "NA");
			}
		}
		else{
			if (!en1.equals("NULL") && !en1.equals("UNK") && !en2.equals("NULL") && !en2.equals("UNK")
					&& !en1.equals("NO") && !en2.equals("NO")){
				featureSet.put("ID_EN", "NO");
			}
			else{
				featureSet.put("ID_EN", "NA");
			}
		}	
		
		//-------------------- Récupération du degré de définition des deux mentions --------------------
		String def1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='DEF']//text()").evaluateFirst(document)).getTextNormalize();
		String def2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='DEF']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_DEF", def1);
		featureSet.put("M2_DEF", def2);
		//-------------------- Vérification de l'identité des locuteurs des deux mentions --------------------
		if (def1.equals(def2)){
			if (!def1.equals("NULL") && !def1.equals("UNK") && !def2.equals("NULL") && !def2.equals("UNK")){
				featureSet.put("ID_DEF", "YES");
			}
			else{
				featureSet.put("ID_DEF", "NA");
			}
		}
		else{
			if (!def1.equals("NULL") && !def1.equals("UNK") && !def2.equals("NULL") && !def2.equals("UNK")){
				featureSet.put("ID_DEF", "NO");
			}
			else{
				featureSet.put("ID_DEF", "NA");
			}
		}
		//-------------------- Récupération de la valeur de l'attribut NEW des deux mentions --------------------
		String new1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='NEW']//text()").evaluateFirst(document)).getTextNormalize();
		String new2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='NEW']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_NEW", new1);
		featureSet.put("M2_NEW", new2);
		//-------------------- Vérification de l'identité des locuteurs des deux mentions --------------------
		if (new1.equals(new2)){
			if (!new1.equals("NULL") && !new1.equals("UNK") && !new2.equals("NULL") && !new2.equals("UNK")){
				featureSet.put("ID_NEW", "YES");
			}
			else{
				featureSet.put("ID_NEW", "NA");
			}
		}
		else{
			if (!new1.equals("NULL") && !new1.equals("UNK") && !new2.equals("NULL") && !new2.equals("UNK")){
				featureSet.put("ID_NEW", "NO");
			}
			else{
				featureSet.put("ID_NEW", "NA");
			}
		}
		//-------------------- Récupération de la valeur du token précédent des deux mentions --------------------
		String previous1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='PREVIOUS']//text()").evaluateFirst(document)).getTextNormalize();
		String previous2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='PREVIOUS']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_PREVIOUS", previous1);
		featureSet.put("M2_PREVIOUS", previous2);
		//-------------------- Vérification de l'identité des locuteurs des deux mentions --------------------
		if (previous1.equals(previous2)){
			featureSet.put("ID_PREVIOUS", "YES");
		}
		else{
			featureSet.put("ID_PREVIOUS", "NO");
		}
		//-------------------- Récupération de la valeur du token suivant des deux mentions --------------------
		String next1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='NEXT']//text()").evaluateFirst(document)).getTextNormalize();
		String next2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='NEXT']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_NEXT", next1);
		featureSet.put("M2_NEXT", next2);
		//-------------------- Vérification de l'identité des locuteurs des deux mentions --------------------
		if (next1.equals(next2)){
			featureSet.put("ID_NEXT", "YES");
		}
		else{
			featureSet.put("ID_NEXT", "NO");
		}
		//-------------------- Récupération des genre et nombre des deux mentions --------------------
		String genre1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='GENRE']//text()").evaluateFirst(document)).getTextNormalize();
		String genre2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='GENRE']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_GENRE", genre1);
		featureSet.put("M2_GENRE", genre2);
		//-------------------- Vérification de l'identité des genre et nombre des deux mentions --------------------
		String nb1 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str1+"']//characterisation//featureSet//feature[@name='NB']//text()").evaluateFirst(document)).getTextNormalize();
		String nb2 = ((Text)XPathFactory.instance().compile("//unit[@id='" +str2+"']//characterisation//featureSet//feature[@name='NB']//text()").evaluateFirst(document)).getTextNormalize();
		featureSet.put("M1_NOMBRE", nb1);
		featureSet.put("M2_NOMBRE", nb2);
		if (genre1.equals(genre2)){
			if (!genre1.equals("NULL") && !genre1.equals("UNK") && !genre2.equals("NULL") && !genre2.equals("UNK")){
				featureSet.put("ID_GENRE", "YES");
			}
			else{
				featureSet.put("ID_GENRE", "NA");
			}
		}
		else{
			if (!genre1.equals("NULL") && !genre1.equals("UNK") && !genre2.equals("NULL") && !genre2.equals("UNK")){
				featureSet.put("ID_GENRE", "NO");
			}
			else{
				featureSet.put("ID_GENRE", "NA");
			}
		}
		if (nb1.equals(nb2)){
			if (!nb1.equals("NULL") && !nb1.equals("UNK") && !nb2.equals("NULL") && !nb2.equals("UNK")){
				featureSet.put("ID_NOMBRE", "YES");
			}
			else{
				featureSet.put("ID_NOMBRE", "NA");
			}
		}
		else{
			if (!nb1.equals("NULL") && !nb1.equals("UNK") && !nb2.equals("NULL") && !nb2.equals("UNK")){
				featureSet.put("ID_NOMBRE", "YES");
			}
			else{
				featureSet.put("ID_NOMBRE", "NA");
			}
		}
		
		//------------------- Vérification de l'intégration d'une des mentions dans l'autre --------------------
		List<Element> parents1 = (List<Element>)(Object)XPathFactory.instance().compile("//anchor[@id='" +str1+"']/ancestor::node()[local-name()='anchor']").evaluate(document);
		List<Element> parents2 = (List<Element>)(Object)XPathFactory.instance().compile("//anchor[@id='" +str2+"']/ancestor::node()[local-name()='anchor']").evaluate(document);
		featureSet.put("EMBEDDED", "NO");
		boolean embedded = false;
		for (Element parent : parents1){
				if (parent.getAttributeValue("id").equals(str2)){
					embedded = true;
					featureSet.put("EMBEDDED", "YES");
				}
		}
		for (Element parent : parents2){
			if (parent.getAttributeValue("id").equals(str1)){
				embedded = true;
				featureSet.put("EMBEDDED", "YES");
			}
		}
		//------------------- Intégration des valeurs de distances entre les deux mentions --------------------
		CalculDistance test = new CalculDistance(document);
		int nbMention = test.NbAnchor(str1, str2);
		int nbTurn = test.NbTurn(str1, str2);
		if (embedded == false){
			List<Integer> distances = test.Count(str1, str2, test.readFileAsString(file));
			if (!distances.isEmpty()){
				featureSet.put("DISTANCE_CHAR", Integer.toString(distances.get(0)));
				featureSet.put("DISTANCE_WORD", Integer.toString(distances.get(1)));
			}
		}
		else{
			featureSet.put("DISTANCE_CHAR", Integer.toString(0));
			featureSet.put("DISTANCE_WORD", Integer.toString(1));
		}
		featureSet.put("DISTANCE_TURN", Integer.toString(nbTurn));
		featureSet.put("DISTANCE_MENTION", Integer.toString(nbMention));

		//------------------- Intégration de la valeur de la classe de la paire (le paramètre classe de la méthode FeatureSearch)--------------------
		//featureSet.put("classe", classe);
		if (classe.equals("YES")){
			temp.add("COREF");
		}
		else{
			temp.add("NOT_COREF");
		}
		this.metadata.add(temp);
		this.instances.add(featureSet);
	}
	
	public void CleanContent(Document document){
		List<Element> listeNext = (List<Element>)(Object)(XPathFactory.instance().compile("//unit//characterisation//featureSet//feature[@name='CONTENT']").evaluate(document));
		for (Element content : listeNext){
			String contenu = content.getValue();
			int compteur = 0;
			while (compteur<contenu.length()){
				char caractere = contenu.charAt(compteur);
				if (caractere=='"'){
					contenu = contenu.substring(0,compteur)+contenu.substring(compteur+1, contenu.length());
				}
				compteur++;
			}
			if (contenu.charAt(0)=='\''){
				contenu = contenu.substring(1, contenu.length());
			}
			content.setText(contenu);
		}
	}
	
	public void OutputArff (String a) throws IOException{
		FileWriter fstream = new FileWriter(a, true);
	    BufferedWriter outFile  = new BufferedWriter(fstream);
		outFile.write("@RELATION coreference\n@ATTRIBUTE M2_FORM string\n@ATTRIBUTE M1_PREVIOUS string\n@ATTRIBUTE M2_NOMBRE {SG,PL,UNK,NULL}\n@ATTRIBUTE M2_EN {NO,PERS,FONC,LOC,ORG,PROD,TIME,AMOUNT,EVENT,NULL,UNK}\n@ATTRIBUTE DISTANCE_MENTION real\n@ATTRIBUTE COM_RATE real\n@ATTRIBUTE M1_DEF {INDEF,EXPL,DEF_DEM,DEF_SPLE,NULL,UNK}\n@ATTRIBUTE M2_PREVIOUS string\n@ATTRIBUTE ID_PREVIOUS {YES,NO,NA}\n@ATTRIBUTE ID_GENRE {YES,NO,NA}\n@ATTRIBUTE ID_FORM {YES,NO,NA}\n@ATTRIBUTE DISTANCE_TURN real\n@ATTRIBUTE M2_TYPE {N,P,PR,NULL,UNK}\n@ATTRIBUTE DISTANCE_WORD real\n@ATTRIBUTE DISTANCE_CHAR real\n@ATTRIBUTE M2_NEXT string\n@ATTRIBUTE M1_FORM string\n@ATTRIBUTE M2_SPK string\n@ATTRIBUTE M2_GENRE {M,F,UNK,NULL}\n@ATTRIBUTE EMBEDDED {YES,NO,NA}\n@ATTRIBUTE M1_GENRE {M,F,UNK,NULL}\n@ATTRIBUTE ID_SPK {YES,NO,NA}\n@ATTRIBUTE INCL_RATE real\n@ATTRIBUTE ID_SUBFORM {YES,NO,NA}\n@ATTRIBUTE ID_EN {YES,NO,NA}\n@ATTRIBUTE M2_NEW {YES,NO,UNK,NULL}\n@ATTRIBUTE ID_NEW {YES,NO,NA}\n@ATTRIBUTE ID_TYPE {YES,NO,NA}\n@ATTRIBUTE M1_EN {NO,PERS,FONC,LOC,ORG,PROD,TIME,AMOUNT,EVENT,NULL,UNK}\n@ATTRIBUTE ID_NEXT {YES,NO,NA}\n@ATTRIBUTE ID_NOMBRE {YES,NO,NA}\n@ATTRIBUTE M1_TYPE {N,P,PR,NULL,UNK}\n@ATTRIBUTE M1_SPK string\n@ATTRIBUTE M1_NEW {YES,NO,UNK,NULL}\n@ATTRIBUTE M1_NEXT string\n@ATTRIBUTE M2_DEF {INDEF,EXPL,DEF_DEM,DEF_SPLE,NULL,UNK}\n@ATTRIBUTE ID_DEF {YES,NO,NA}\n@ATTRIBUTE M1_NOMBRE {SG,PL,UNK,NULL}\n@ATTRIBUTE class {COREF, NOT_COREF}");
        outFile.write("\n\n@data\n");
        int index = 0;
        for (Hashtable<String,String> table : this.instances){
        	List<String> meta = this.metadata.get(index);
			Iterator<String> itKey = table.keySet().iterator();
			while(itKey.hasNext()){
				String key = itKey.next();
				if (!key.equals("class") && !key.equals("antecedent") && !key.equals("reprise")){
					if (!key.equals("M1_FORM") && !key.equals("M2_FORM") && !key.equals("M1_PREVIOUS") &&
							!key.equals("M2_PREVIOUS") && !key.equals("M1_NEXT") && !key.equals("M2_NEXT")){
						outFile.write(table.get(key)+" ");
					}
					else{
						String aCopier = table.get(key);
						if (table.get(key).contains("\"")){
							aCopier = aCopier.replace("\"","^");
						}
						outFile.write("\""+aCopier+"\" ");
					}
				}
			}
			outFile.write(meta.get(2)+" ");
        	outFile.write("% "+meta.get(0)+" ");
        	outFile.write(meta.get(1));
        	
        	outFile.write("\n");
    		index++;
        }
        if (this.mode.equals("test")){
	        outFile.write("\n% BEGIN REFERENCE\n");
	        for (List<Integer> entity : this.chainListNum){
	        	outFile.write("%");
	        	for (Integer i : entity){
	        		outFile.write(" "+i);
	        	}
	        	outFile.write("\n");
	        }
	        outFile.write("% END REFERENCE");
        }
        
        outFile.close();
	}
}
