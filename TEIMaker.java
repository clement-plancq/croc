import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathFactory;
@SuppressWarnings("unchecked")

public class TEIMaker {
	private Document document = new Document();
	private Document docOutput = new Document();
	private String modelName = new String();
	private String dirName = new String();
	private Element body = new Element("body");
	private Element text = new Element("text");
	private Element back = new Element("back");
	private Element spanTurn = new Element("spanGrp");
	private Element spanMaillon = new Element("spanGrp");
	private Element joinGrp = new Element("joinGrp");
	private Element fvLib = new Element("fvLib");
	private static Namespace xml = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
	private static Namespace xmlns = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
	
	public TEIMaker (Document doc, String outPut, String dir, String setType, String model) throws IOException{
		this.dirName = dir;
		this.document = doc;
		this.modelName = model;
		this.docOutput.addContent(new Element("teiCorpus"));
		this.docOutput.getRootElement().setNamespace(xmlns);
		this.docOutput.getRootElement().addContent(new Element("TEI"));
		this.docOutput.getRootElement().getChild("TEI").addContent(text);
		this.CorpusData(outPut, setType);
	}
	
	public void CorpusData(String fileName, String setType) throws IOException{
		text.addContent(this.body);
		text.addContent(this.back);
		spanTurn.setAttribute("type", "AnalecUnit"); spanTurn.setAttribute("n", "Tour"); 
		back.addContent(spanTurn);
		spanMaillon.setAttribute("type", "AnalecUnit"); spanMaillon.setAttribute("n", "Maillon"); 
		back.addContent(spanMaillon);
		joinGrp.setAttribute("type","AnalecSchema"); joinGrp.setAttribute("n","Coreference");
		back.addContent(joinGrp);
		fvLib.setAttribute("n","AnalecElementProperties");
		back.addContent(fvLib);
		
		List<Element> listTurn = (List<Element>)(Object)XPathFactory.instance().compile("//Turn").evaluate(this.document);
		for (Element tour : listTurn){
			// Génération des éléments contenus sous le back
			Element span = new Element("span");
			span.setAttribute("id", "u-Tour-"+tour.getAttributeValue("id"),xml);
			span.setAttribute("from", "#u-Tour-"+tour.getAttributeValue("id")+"-start");
			span.setAttribute("to", "#u-Tour-"+tour.getAttributeValue("id")+"-end");
			span.setAttribute("ana", "#u-Tour-"+tour.getAttributeValue("id")+"-fs");
			spanTurn.addContent(span);
			
			// Génération des éléments fs décrivant les features
			Element fsTurn = new Element("fs");
			fsTurn.setAttribute("id", "u-Tour-"+tour.getAttributeValue("id")+"-fs",xml);
			fvLib.addContent(fsTurn);
			Element start = new Element("f");
			start.setAttribute("name","startTime");
			Element stringStart = new Element("string");
			stringStart.addContent(tour.getAttributeValue("startTime"));
			start.addContent(stringStart);
			fsTurn.addContent(start);
			Element end = new Element("f");
			end.setAttribute("name","endTime");
			Element stringEnd = new Element("string");
			stringEnd.addContent(tour.getAttributeValue("endTime"));
			end.addContent(stringEnd);
			fsTurn.addContent(end);
			// Génération des éléments contenus sous le body
			Element paragraph = new Element("p");
			Element turn = new Element("anchor");
			turn.setAttribute("id", "u-Tour-"+tour.getAttributeValue("id")+"-start",xml);
			turn.setAttribute("type", "AnalecDelimiter");
			turn.setAttribute("subType", "UnitStart");
			paragraph.addContent(turn);
			List<Content> enfants = tour.getContent();
			for (Content enfant : enfants){
				if (enfant.getCType().toString().equals("Element")){
					Element element = (Element)enfant;
					if (element.getName().equals("anchor")){
						//Element nouveau = new Element(element.getName());
						this.RecursiveAnchor(element, paragraph);
					}
					if (element.getName().equals("Who")){
						paragraph.addContent(" ");
					}
				}
				else{
					Text contenu = (Text)enfant;
					paragraph.addContent(contenu.getTextNormalize()+" ");
				}
			}
			Element endTurn = new Element("anchor");
			endTurn.setAttribute("id", "u-Tour-"+tour.getAttributeValue("id")+"-end", xml);
			endTurn.setAttribute("type", "AnalecDelimiter");
			endTurn.setAttribute("subType", "UnitEnd");
			paragraph.addContent(endTurn);
			body.addContent(paragraph);
		}
		this.AnnotationData();
		XMLOutputter xmlOutput = new XMLOutputter();
		if (setType.equals("system")){
			xmlOutput.output(this.docOutput, new FileWriter(dirName+"/TEI_"+this.modelName+"_"+setType+"_"+fileName));
		}
		else{
			xmlOutput.output(this.docOutput, new FileWriter(dirName+"/TEI_"+setType+"_"+fileName));
		}
	}
	
	public void AnnotationData(){
		List<Element> listChaine = (List<Element>)(Object)XPathFactory.instance().compile("//chaine").evaluate(this.document);
		for (Element chaine : listChaine){
			Element join = new Element("join");
			join.setAttribute("id", "s-Coréférence-"+chaine.getAttributeValue("id"), xml);
			List<Element> listeMaillons = chaine.getChildren("maillon");
			String targetValue = "";
			for(Element maillon : listeMaillons){
				targetValue = targetValue+"#u-Maillon-"+maillon.getAttributeValue("id")+" ";
			}
			join.setAttribute("target", targetValue);
			join.setAttribute("ana", "#s-Coréférence-"+chaine.getAttributeValue("id")+"-fs");
			joinGrp.addContent(join);
			
			// Génération des métadonnées des chaînes de coréférence
			Element fsCoref = new Element("fs");
			fsCoref.setAttribute("id", "s-Coréférence-"+chaine.getAttributeValue("id")+"-fs",xml);
			fvLib.addContent(fsCoref);
		}
	}
	
	public void RecursiveAnchor(Element anchor, Element paragraph){
		Element span = new Element("span");
		span.setAttribute("id", "u-Maillon-"+anchor.getAttributeValue("num"), xml);
		span.setAttribute("from", "#u-Maillon-"+anchor.getAttributeValue("num")+"-start");
		span.setAttribute("to", "#u-Maillon-"+anchor.getAttributeValue("num")+"-end");
		span.setAttribute("ana", "#u-Maillon-"+anchor.getAttributeValue("num")+"-fs");
		spanMaillon.addContent(span);
		
		Element maillon = new Element("anchor");
		maillon.setAttribute("id", "u-Maillon-"+anchor.getAttributeValue("num")+"-start", xml);
		maillon.setAttribute("type", "AnalecDelimiter");
		maillon.setAttribute("subType", "UnitStart");
		paragraph.addContent(maillon);
		
		Element fsAnchor = new Element("fs");
		fsAnchor.setAttribute("id", "u-Maillon-"+anchor.getAttributeValue("num")+"-fs",xml);
		fvLib.addContent(fsAnchor);
		// Métadonées des unités référentielles
		List<Element> informations = (List<Element>)(Object)XPathFactory.instance().compile("//unit[@id='" +anchor.getAttributeValue("id")+"']/characterisation/featureSet/feature").evaluate(this.document);
		for (Element info : informations){;
			Element f = new Element("f");
			f.setAttribute("name",info.getAttributeValue("name"));
			Element stringF = new Element("string");
			stringF.addContent(info.getTextNormalize());
			f.addContent(stringF);
			fsAnchor.addContent(f);
		}
		// Type de l'unité référentielle (POS)
		Element type = (Element)XPathFactory.instance().compile("//unit[@id='" +anchor.getAttributeValue("id")+"']/characterisation/type").evaluateFirst(this.document);
		Element pos = new Element("f");
		pos.setAttribute("name","type");
		Element stringType = new Element("string");
		stringType.addContent(type.getTextNormalize());
		pos.addContent(stringType);
		fsAnchor.addContent(pos);
		
		
		List<Content> enfants = anchor.getContent();
		for (Content enfant : enfants){
			if (enfant.getCType().toString().equals("Element")){
				Element element = (Element)enfant;
				if (element.getName().equals("anchor")){
					//Element nouveau = new Element(element.getName());
					//body.addContent(nouveau);
					this.RecursiveAnchor(element, paragraph);
				}
			}
			else{
				Text contenu = (Text)enfant;
				paragraph.addContent(contenu.getTextNormalize()+" ");
			}
		}
		Element endMaillon = new Element("anchor");
		endMaillon.setAttribute("id", "u-Maillon-"+anchor.getAttributeValue("num")+"-end", xml);
		endMaillon.setAttribute("type", "AnalecDelimiter");
		endMaillon.setAttribute("subType", "UnitEnd");
		paragraph.addContent(endMaillon);
	}
}
