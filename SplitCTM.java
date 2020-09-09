
import java.io.*;
import java.util.regex.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.*;

/**
 *
 * @author ADMIN
 */
public class SplitCTM {
	public static void makeMap(Vector<String> listUtt, HashMap<String, BufferedWriter> h, Vector<BufferedWriter> listBuff, int numOfJobs, String outfilePrefix, String outdir) {
		try
		{
			int avgUttPerJob = listUtt.size()/numOfJobs;
			for(int i = 0; i < numOfJobs;i++)
			{
				
				FileWriter fstream1 = new FileWriter(outdir + "/" + outfilePrefix + "." + i,false);
        		BufferedWriter bw1 = new BufferedWriter(fstream1);
                listBuff.add(bw1);
        		
			}
            for(int i = 0; i < listUtt.size();i++) {
                int index = i/avgUttPerJob;
				if(index >= numOfJobs) index = numOfJobs - 1;
                h.put(listUtt.elementAt(i), listBuff.elementAt(index));
            }
			
        	
        } 
        catch(Exception ex) {
        }
	}

    public static void splitCTM(String ctm, int numOfJobs, String outfilePrefix, String outdir, Vector<String> listUtt)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(ctm);
        	DataInputStream in = new DataInputStream(fstream);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));

            HashMap<String, BufferedWriter> h = new HashMap<String, BufferedWriter>();
            Vector<BufferedWriter> listBuff = new Vector<BufferedWriter>();
            makeMap(listUtt, h, listBuff, numOfJobs,  outfilePrefix, outdir);
        	String strLine;
        	while ((strLine = br.readLine()) != null)   {
				StringTokenizer token = new StringTokenizer(strLine);
				String segId = token.nextToken();
				BufferedWriter bw = h.get(segId);
                bw.write(strLine);
                bw.newLine();
        	}
			br.close();
            for(int i = 0; i < listBuff.size();i++) {
                BufferedWriter bw = listBuff.elementAt(i);
                bw.close();
            }
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }

	
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
					if (label.contains("_"))
						label = label.substring(0, label.lastIndexOf("_")); 
					word = label;
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
		int numJob = Integer.parseInt(args[2]);
		String outfolder = args[3];
		
		HashMap<String, Segment > id2SegWord = new HashMap<String, Segment >();
		Vector<String> listUtt1 = new Vector<String>();
		loadCTM(ctm_word, id2SegWord, null, listUtt1);
		System.out.println("Load the word-based CTM succesfully");
		splitCTM(ctm_word, numJob, "ctm_word", outfolder, listUtt1);
		System.out.println("Finish ctm word");
        splitCTM(ctm_subword, numJob, "ctm_subword", outfolder, listUtt1);
		
	}

}

