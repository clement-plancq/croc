import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
 * Cette classe est utile lorsque l'utilisateur choisit de générer un modèle
 * selon son propre ensemble de traits.
 * A partir d'un fichier contenant un chiffre par ligne, la méthode GetFeatureFromFile
 * retourne un tableau représentant l'ensemble des traits à supprimer pour l'apprentissage
 * 
 *  REMARQUE : L'API de Weka possède une fonction pour filtrer les traits à conserver
 *  		   parmi un ensemble initial.
 *  		   Le paramètre à fournir à cette méthode est un tableau int[] contenant les éléments
 *  		   à supprimer, et non ceux à conserver
 */

public class FeatureToList {
	
	public int[] GetFeatureFromFile(String featureSet) throws IOException{
		int[] array = {};
		List<Integer> firstList = new ArrayList<Integer>();
		BufferedReader lecteur= null;
		try{
			lecteur = new  BufferedReader(new FileReader(featureSet));
			String ligne = null;
			try{
				while ((ligne = lecteur.readLine()) != null){
					firstList.add( Integer.parseInt(ligne));
				}
			}
			catch (java.io.IOException e){
			}
			lecteur.close();
		}
		catch (Exception e){
			System.out.println(e.toString());
		}
		
		for (Integer nb : firstList){
			if (nb>38){
				firstList.remove(nb);
			}
		}
		
		List<Integer> temp = new ArrayList<Integer>();
		List<Integer> stringFtList = new ArrayList<Integer>();
		stringFtList.add(0);stringFtList.add(1);stringFtList.add(7);
		stringFtList.add(15);stringFtList.add(16);stringFtList.add(17);
		stringFtList.add(32);stringFtList.add(34);
		List<Integer> completeList =  new ArrayList<Integer>();
		int i = 0;
		while (i<38){
			completeList.add(i+1);
			i++;
		}
		for (Integer j : completeList){
			if (!firstList.contains(j)){
				temp.add(j-1);
			}
		}
		for (Integer m : stringFtList){
			if(!temp.contains(m)){
				temp.add(m);
			}
		}
		for (Integer x : temp){
			array = this.addElement(array, x);
		}
		return array;
	}
	
	public int[] addElement(int[] a, int e) {
	    a  = Arrays.copyOf(a, a.length + 1);
	    a[a.length - 1] = e;
	    return a;
	}
}
