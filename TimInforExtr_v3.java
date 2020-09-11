
import java.io.*;
import java.util.regex.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.*;

/**
 *
 * @author ADMIN
 */
public class TimInforExtr {

	public static Vector<String> readFile(String text)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(text);
        	DataInputStream in = new DataInputStream(fstream);

        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	String strLine;
        	Vector<String> v = new Vector<String>();
        	while ((strLine = br.readLine()) != null)   {
            	v.add(strLine);
        	}
			br.close();
			return v;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	public static void loadWord2Subword(String w2sMap, HashMap<String, Vector<String>> h)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(w2sMap);
        	DataInputStream in = new DataInputStream(fstream);

        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	String strLine;
        	

        	while ((strLine = br.readLine()) != null)   {
            	StringTokenizer token = new StringTokenizer(strLine);
				String infor[] = strLine.split("\\s+");
				String pron = "";
				for(int i = 1; i < infor.length;i++) 
					pron = pron + " " + infor[i];
				pron = pron.trim();
				if(h.containsKey(infor[0]))
				{
					Vector<String> listPron = h.get(infor[0]);
					listPron.add(pron);
				}
				else
				{
					Vector<String> listPron = new Vector<String>();
					listPron.add(pron);
                    h.put(infor[0], listPron);
				}
        	}
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
	
	public static double round(double value, int places) {
        	if (places < 0) throw new IllegalArgumentException();

        	long factor = (long) Math.pow(10, places);
        	value = value * factor;
        	long tmp = Math.round(value);
        	return (double) tmp / factor;
    }

	public static void loadMap(String file, HashMap<String, String> h) 
	{
		try
		{
			FileInputStream fstream = new FileInputStream(file);
        	DataInputStream in = new DataInputStream(fstream);

        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	String strLine;
        	while ((strLine = br.readLine()) != null)   {
				String info[] = strLine.split("\\s+");
				
				h.put(info[1], info[0]);
        	}
			br.close();
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

	public static void process(HashMap<String, Segment > id2SegWord, HashMap<String, Segment > id2SegSubword, HashMap<String, Vector<String>> word2Prons, Vector<String> listUtt, String fileOut) 
	{
		try
		{

			FileWriter fstream1 = new FileWriter(fileOut,false);
        	BufferedWriter bw1 = new BufferedWriter(fstream1);
        	
        	for(int i = 0; i < listUtt.size();i++) {
        		String utt = listUtt.elementAt(i);
                System.out.println("Process utt = " + utt);
        		Segment ws = id2SegWord.get(utt);
        		Segment subS = id2SegSubword.get(utt);
                if(subS == null) {
                    System.out.print("Cannot find utt " + utt + " in subword ctm ");
                }
        		findTimeInfor(ws, subS, word2Prons);
        		for(int j = 0; j < ws.list.size();j++) {
        			WordItem w = ws.list.elementAt(j);
        			String pron = ws.pron.elementAt(j);
        			String timeInfor = ws.timeInfor.elementAt(j);
        			bw1.write(w.id + " " + w.segment + " " + w.start + " " + w.end + " " + pron + " " + timeInfor);
        			bw1.newLine();
        		}
        	}
            bw1.close();
        } 
        catch(Exception ex) {
            ex.printStackTrace();
        }
	}
	public static void findTimeInfor(Segment segW, Segment sub, HashMap<String, Vector<String>> word2Prons) {
		int currPos = 0; //Current position in the subword sequence
		Vector<String> listPron = new Vector<String>();
		Vector<String> listTimeInfor = new Vector<String>();
        Vector<String> listProcessedWord = new Vector<String>();
        boolean post_process = false;
		for(int i = 0; i < segW.list.size();i++) {
			WordItem item = segW.list.elementAt(i);
			Vector<String> prons = word2Prons.get(item.id);
            if(!post_process)
                Collections.sort(prons, Collections.reverseOrder());
            else 
                Collections.sort(prons);
            if(prons == null) {
                System.out.println("Cannot find pronunciation for word " + item.id);
                System.exit(1);
            }
			boolean isOk = false;
			for(int j = 0; j < prons.size();j++) {
				String pron = prons.elementAt(j);
				String result =  find(sub, currPos, pron);
				int numPhone = result.split("\\s+").length;
				if(result.length() > 0) {
					currPos = currPos + numPhone;
					isOk = true;
					//v.add(result);
					listPron.add(pron);
					listTimeInfor.add(result);
                    listProcessedWord.add(item.id);
                    post_process = false;
					break;
				}
			}
			if (!isOk && post_process) {
				System.out.println("Cannot find time information for " + segW.segment + " at word " + item.id + " (position = " + i + ")");
                System.out.println("Word content = " + segW.getContent());
                System.out.println("Subword content = " + sub.getContent());
                String onePron = prons.elementAt(0);
                int numPhone = onePron.split("\\s+").length;
                System.out.println("currPos = " + currPos + ". Expected something like " + onePron + ", but observed subword sequence " + sub.getPartialContent(currPos, numPhone));
                System.out.println("Processed sequence =");
                for(int j = 0; j < listProcessedWord.size();j++) {
                    System.out.println(listProcessedWord.elementAt(j) + "-->" + listPron.elementAt(j));
                }
                System.out.println("The rest are:" + sub.getPartialContent(currPos, sub.list.size()-currPos-1));
				System.exit(1);
			}
            else if (!isOk && !post_process) {
                i = i -2;
                listPron.remove(listPron.size()-1);
                listTimeInfor.remove(listTimeInfor.size()-1);
                listProcessedWord.remove(listProcessedWord.size()-1);
                post_process = true;
            }
			
		}
		segW.pron = listPron;
		segW.timeInfor = listTimeInfor;
		
	}
	
	public static String find(Segment sub, int currPos, String pron) {
		String subSeq[] = pron.split("\\s+");
		String result = "";
        //if(sub == null) { System.out.println("WTF");}
		for(int i = 0; i < subSeq.length;i++) {
            if(i + currPos >= sub.list.size()) {
                //System.out.println("Length mismatch expected! i = " + i + ", currPos = " + currPos + ", pron = " + pron + ", utt = " + sub.segment + ", sub sequence = " + sub.getContent() + ",  subword sequence length = " + sub.list.size());
                return "";
            }
			WordItem item = sub.list.elementAt(i + currPos);
            
			if(item.id.equals(subSeq[i])) {
				result = result + " " + item.start + "," + item.end;
			}
			else {
				return "";
			}
		}
		return result.trim();
	}
	
	/*public static void loadCTM(String ctm, HashMap<String, Segment > id2Seg, HashMap<String, String> id2Label, Vector<String> listUtt)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(ctm);
        	DataInputStream in = new DataInputStream(fstream);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));

        	String strLine;
        	Vector<String> v = new Vector<String>();
			String prevSegment = "";
			WordItem previousWord = null;
			int count = 0;
        	while ((strLine = br.readLine()) != null)   {
				StringTokenizer token = new StringTokenizer(strLine);
				String segId = token.nextToken();
				token.nextToken();
				String start = token.nextToken();
				String end = token.nextToken();
				String word = token.nextToken();
				if (id2Label != null) {
					String label = id2Label.get(word);
					if (label.contains("_"))
						label = label.substring(0, label.lastIndexOf("_")); 
					word = label;
				}
            	WordItem w = new WordItem(word, start, end, segId);

				if(id2Seg.containsKey(segId))
				{
					Segment seg = id2Seg.get(segId);
					seg.list.add(w);
				}
				else
				{
					Segment seg = new Segment();
					seg.segment = segId;
					seg.list.add(w);
				}
				if(!listUtt.contains(segId)) listUtt.add(segId);
				count++;
				if (count % 40000 == 0) {
				System.out.println("---- Finish " + count + " entries");
				}
        	}
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}*/

    	public static void loadCTM(String ctm, HashMap<String, Segment > id2Seg, HashMap<String, String> id2Label, Vector<String> listUtt)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(ctm);
        	DataInputStream in = new DataInputStream(fstream);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));

        	String strLine;
        	Vector<String> v = new Vector<String>();
			String prevSegment = "";
			WordItem previousWord = null;
			int count = 0;
            String prevSeg = "";
        	while ((strLine = br.readLine()) != null)   {
				StringTokenizer token = new StringTokenizer(strLine);
				String segId = token.nextToken();
				token.nextToken();
				String start = token.nextToken();
				String end = token.nextToken();
				String word = token.nextToken();
				if (id2Label != null) {
					String label = id2Label.get(word);
                    if(label == null) {
                        System.out.println("Cannot find label for the id " + word);
                    }
					if (label.contains("_"))
						label = label.substring(0, label.lastIndexOf("_")); 
					word = label;
				}
                if(word.equals("sil")) {
                    if(!prevSeg.equals(segId)) { 
                        listUtt.add(segId);
                        Segment seg = new Segment();
					    seg.segment = segId;
                        id2Seg.put(segId, seg);
                    }
                    prevSeg = segId;
				    count++;
                    continue;
                }
            	WordItem w = new WordItem(word, start, end, segId);

				if(prevSeg.equals(segId))
				{
					Segment seg = id2Seg.get(segId);
					seg.list.add(w);
				}
				else
				{
					Segment seg = new Segment();
					seg.segment = segId;
					seg.list.add(w);
                    id2Seg.put(segId, seg);
				}
                
				if(!prevSeg.equals(segId)) listUtt.add(segId);
                prevSeg = segId;
				count++;
				if (count % 40000 == 0) {
				System.out.println("---- Finish " + count + " entries");
				}
        	}
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void main(String args[])
	{
		String ctm_subword = args[0];
		String ctm_word = args[1];
		String lexicon = args[2];
		String phonesFile = args[3];
		String outFile = args[4];
		
		HashMap<String, Segment > id2SegWord = new HashMap<String, Segment >();
		HashMap<String, Segment > id2SegSubword = new HashMap<String, Segment >();
		HashMap<String, String> id2Label = new HashMap<String, String>();
		loadMap(phonesFile, id2Label);
		System.out.println("Load the mapping from phone id to label succesfully");
		HashMap<String, Vector<String>> w2pron = new HashMap<String, Vector<String>>();
		loadWord2Subword(lexicon, w2pron);
		System.out.println("Load the dictionary succesfully");
		Vector<String> listUtt1 = new Vector<String>();
		Vector<String> listUtt2 = new Vector<String>();
		loadCTM(ctm_word, id2SegWord, null, listUtt1);
		System.out.println("Load the word-based CTM succesfully");
		loadCTM(ctm_subword, id2SegSubword, id2Label, listUtt2);
		System.out.println("Load the subword CTM succesfully");
		process(id2SegWord, id2SegSubword, w2pron, listUtt1, outFile);
		System.out.println("Finish");
		
	}

}


class WordItem
{
	String id;
	String start;
	String end;
	String segment;

	public WordItem(String _id, String _st, String _end, String _segment)
	{
		id = _id;
		start = _st;
		end = _end;
		segment = _segment;
	}
}


class Segment
{
	String segment;
	String trans;
	Vector<WordItem> list;
	Vector<String> pron; //Only used when we are working on segment of words
	Vector<String> timeInfor; //Only used when we are working on segment of words
	public Segment()
	{
		trans = "";
		list = new Vector<WordItem>();
		segment = "";
	}
    public String getContent() {
        String s = "";
        for(int i = 0; i < list.size(); i++) {
            WordItem w = list.elementAt(i);
            s = s + w.id + " ";
        }
        s = s.trim();
        return s;
    }
    public String getPartialContent(int p, int l) {
        String s = "";
        for(int i = p; i < p + l; i++) {
            WordItem w = list.elementAt(i);
            s = s + w.id + " ";
        }
        s = s.trim();
        return s;
    }
}

