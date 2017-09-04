import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;

/*
 * Cette classe permet de calculer différents degrés de distance entre deux unités référentielles.
 * Elle a deux accesseurs :
 * 	-> Un premier nécessitant en paramètre un document DOM
 * 	-> Un second sans paramètre
 * 
 * Différentes méthodes sont utiles à chaque niveau de distance :
 * 	-> La méthode NbAnchor calcule la distance en nombre de mentions. Elle prend en paramètre
 * 	   deux chaînes de caractères correspondant aux attributs "id" des éléments "unit" décrivant
 * 	   les deux expressions référentielles à analyser.
 * 	   Le retour de la fonction correspond au nombre de mentions séparant ces deux unités,
 *     que l'on obtient en soustrayant leur valeurs respectives d'attribut "num".
 * -> La méthode NbTurn calcule de la même manière la distance en nombre de tour de parole.
 * 	  Les deux paramètres sont les mêmes, grâce auxquels on récupère les éléments Turn parents de
 *    chacune des deux unités, dont on soustrait les valeurs d'attributs "id".
 * -> La méthode Count propose en sortie une liste de deux éléments, le premier correspondant au
 * 	  nombre de caractères entre deux mentions, le second à celle en nombre de mots.
 * 	  Elle prend 3 paramètres que sont deux nouveaux les deux identifiants des unités, et le fichier xml
 *    en tant que chaîne de caractères. Grâce à une expression régulière, on récupère tout ce qui se
 *    trouve entre les deux identifiants, puis on nettoie cette sortie des éléments XML. La chaîne de
 *    caractères finale ne contient donc que les éléments textuels compris entre les deux mentions. 
 *    La taille de cette chaîne nous donne la distance en nombre de caractères entre ces mentions,
 *    puis après tokenisation, on obtient celle en nombre de mots.
 */

public class CalculDistance {
	private Document document = new Document();
	
	public CalculDistance (Document doc){
		this.document = doc;
	}

	public CalculDistance (){
	}
	
	public int NbAnchor (String str1, String str2) throws IOException{
		Element mention1 = (Element)XPathFactory.instance().compile("//anchor[@id='" +str1+"']").evaluateFirst(this.document);
		Element mention2 = (Element)XPathFactory.instance().compile("//anchor[@id='" +str2+"']").evaluateFirst(this.document);
		int nbAnchor = (Integer.parseInt(mention2.getAttributeValue("num")))- (Integer.parseInt(mention1.getAttributeValue("num")));
		return nbAnchor;
	}
	
	public int NbTurn (String str1, String str2) throws IOException{
		Element turn1 = (Element)XPathFactory.instance().compile("//anchor[@id='" +str1+"']/ancestor::Turn").evaluateFirst(this.document);
		Element turn2 = (Element)XPathFactory.instance().compile("//anchor[@id='" +str2+"']/ancestor::Turn").evaluateFirst(this.document);
		int nbTurn = (Integer.parseInt(turn2.getAttributeValue("id")))- (Integer.parseInt(turn1.getAttributeValue("id")));
		return nbTurn;
	}
	
	public String readFileAsString(String filePath) throws java.io.IOException{
		 byte[] buffer = new byte[(int) new File(filePath).length()];
		 BufferedInputStream f = null;
		 try {
		 f = new BufferedInputStream(new FileInputStream(filePath));
		 f.read(buffer);
		 } finally {
		 if (f != null) try { f.close(); } catch (IOException ignored) { }
		 }
		 return new String(buffer);
		 }
	
	public List<Integer> Count(String str1, String str2, String fichier) throws IOException{
		List<Integer> distance = new ArrayList<Integer>();
		if (this.NbAnchor(str1, str2) > 1){
			Matcher searching1 =  Pattern.compile(str1).matcher(fichier);
			Matcher searching2 = Pattern.compile(str2).matcher(fichier);
			int verif1 = 0;
			int verif2 = 0;
			while (verif1==0){
				searching1.find();
				verif1 = searching1.end();
			}
			while (verif2==0){
				searching2.find();
				verif2 = searching2.start();
			}
			String subPart = fichier.substring(verif1, verif2);
			String copy= "";
			new StringUtils();
			//StringUtils str = new StringUtils();
			subPart = StringUtils.strip(subPart);
			Matcher text =  Pattern.compile("([^\\s]+\\s?[^\\s]*)").matcher(subPart);
			while (text.find()){
				copy = copy + subPart.substring(text.start(),text.end())+" ";
			}
			int close = copy.indexOf("</anchor>");
			int closeBis = close;
			String i = copy.substring(close, close+19);
			while (i.equals("</anchor>")){
				closeBis = closeBis+19;
				i = copy.substring(closeBis, closeBis+19);
			}
			
			int lastAnchor = copy.lastIndexOf("<anchor id=\"");
			copy = copy.substring(closeBis, lastAnchor);
			int verif = 0;
			int begin = 0;
			Matcher balise = Pattern.compile("<[^>]*>").matcher(copy);
			while(balise.find()){
				verif++;
			}
			while(begin<verif){
				copy = this.DeleteTag(copy);
				begin++;
			}
			List<String> listeMots = new ArrayList<String >();
			StringTokenizer st = new StringTokenizer(copy);
			while (st.hasMoreTokens()){
				listeMots.add(st.nextToken());
			}
			int nbMots = 0;
			int nbChar = 0;
			for (String j : listeMots){
				if(!j.contains("<") && !j.contains(">") && !j.contains("=") && !j.equals("\"") ){
					nbMots++;
					nbChar = nbChar + j.length();
				}
			}
			distance.add(nbChar);
			distance.add(nbMots);
		}
		else{
			distance.add(0); distance.add(0);
		}
		return distance;
	}
	
	public String DeleteTag(String fichier){
		int first = 0;
		Matcher balise = Pattern.compile("<[^>]*>").matcher(fichier);
		while (first==0){
			balise.find();
			fichier = fichier.substring(0,balise.start())+fichier.substring(balise.end(), fichier.length());
			first++;
		}
		return fichier;
	}
}
