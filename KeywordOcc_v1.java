
import java.io.*;
import java.util.regex.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.*;

/**
 *
 * @author ADMIN
 */
public class KeywordOcc_v4 {

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
	public static HashMap<String, String> loadWord2Subword(String w2sMap)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(w2sMap);
        	DataInputStream in = new DataInputStream(fstream);

        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	String strLine;
        	HashMap<String, String > w2s = new HashMap<String, String >();

        	while ((strLine = br.readLine()) != null)   {
            	StringTokenizer token = new StringTokenizer(strLine);
				String info[] = strLine.split("=");
				w2s.put(info[0], info[1]);
        	}
			br.close();
			return w2s;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public static HashMap<String, String> audio2gen(String audioGenFile)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(audioGenFile);
        	DataInputStream in = new DataInputStream(fstream);

        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	String strLine;
        	HashMap<String, String > a2g = new HashMap<String, String >();
			br.readLine();
        	while ((strLine = br.readLine()) != null)   {
				String info[] = strLine.split("\\s+");
				
				String au = "";
				if(info[0].contains("."))
				{
					StringTokenizer token = new StringTokenizer(info[0], ".");
					if(token.hasMoreTokens())
						au = token.nextToken();
					else 
					{
						System.out.println("Check audio " + info[0]);
						System.exit(1);
					}
				}
				else 
				{
					System.out.println("Check audio " + info[0]);
					au = info[0];
				}
				a2g.put(au, info[7]);
        	}
			br.close();
			return a2g;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
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
				
				h.put(info[0], info[1]);
        	}
			br.close();
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

	public static void processKWFile(String ctm, String kwlist, String fileOut, String log, String audio2Gen, String segment, String utt2spk)
	{
		try
		{
			HashMap<String, String> a2g = audio2gen(audio2Gen);
			HashMap<String, String> utt2spkMap = new HashMap<String, String>();
			loadMap(utt2spk, utt2spkMap);
			HashMap<String, String> utt2AudioMap = new HashMap<String, String>();
			loadMap(segment, utt2AudioMap);
			HashMap<String, Vector<WordItem> > wordToLocation = new HashMap<String, Vector<WordItem> >();
			Vector<String> listWord = new Vector<String>();
			loadCTM(ctm, wordToLocation, listWord, a2g, utt2AudioMap, utt2spkMap );
	
			FileInputStream fstream = new FileInputStream(kwlist);
        	DataInputStream in = new DataInputStream(fstream);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));

			FileWriter fstream1 = new FileWriter(fileOut,false);
        	BufferedWriter bw1 = new BufferedWriter(fstream1);
			FileWriter fstream2 = new FileWriter(log,false);
        	BufferedWriter bw2 = new BufferedWriter(fstream2);

        	String strLine;
			int count_wholekw = 0;
			int count = 0;
        	Vector<String> v = new Vector<String>();
			
        	while ((strLine = br.readLine()) != null)   {
            	v.add(strLine);
				String kid = strLine.substring(0,strLine.indexOf('\t')).trim();
				String kwContent = strLine.substring(strLine.indexOf('\t')+1);
				System.out.println("Process keyword [" + kid + "] = " + kwContent);
				try {
					String instance = searchAll(kid, kwContent, wordToLocation, listWord, bw1, bw2, utt2AudioMap);
					String info[] = instance.split(",");
					System.out.println("Whole keyword " + kid + " has " + info[0] + 
						" instances");
					if(info[0].equals("0"))
						count_wholekw++;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				//System.out.println("[" + strLine + "] " + s);
        	}
			bw1.close();
			bw2.close();
			System.out.println("There are " + count_wholekw + " keywords having no instance");
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public static String searchAll(String kid, String s, HashMap<String, Vector<WordItem> > wordToLocation, Vector<String> listWord,  BufferedWriter bw1, BufferedWriter bw2, HashMap<String, String> utt2AudioMap) throws Exception
	{
		String strReturn = "";
		String result = "";
		int count = 0;
		String kw[] = s.split("\\s+");
		int numOfWord = kw.length;
		bw2.newLine();
		Vector<Vector<WordItem>> listOccs = new Vector<Vector<WordItem>>();
		for(int i = 0; i < numOfWord;i++) 
		{
			Vector<WordItem> temp;
			if(wordToLocation.containsKey(kw[i])) 
				temp = wordToLocation.get(kw[i]);
			else 
				return (strReturn + count + "," + count);
			System.out.println("Word [" + kw[i] + "] has " + temp.size() + " samples");
			listOccs.add(temp);
		}
		Vector<Result> listRes = new Vector<Result>();
			
		//GeneratePermutations(listOccs, results, 0, strTMP, 1.0, null);
		if(listOccs.size() == 1)
			GeneratePermutationsV1(listOccs, listRes, utt2AudioMap, bw2);
		else if(listOccs.size() == 2)
			GeneratePermutationsV2(listOccs, listRes, utt2AudioMap, bw2);
		else if(listOccs.size() == 3)
			GeneratePermutationsV3(listOccs, listRes, utt2AudioMap, bw2);
		else if(listOccs.size() == 4)
			GeneratePermutationsV4(listOccs, listRes, utt2AudioMap, bw2);
		else if(listOccs.size() == 5)
			GeneratePermutationsV5(listOccs, listRes, utt2AudioMap, bw2);
		else if(listOccs.size() == 6)
			GeneratePermutationsV6(listOccs, listRes, utt2AudioMap, bw2);
		else if(listOccs.size() > 6)
		{
			System.out.println("Keyword " + kid + " is too long ");
			return "0,0";
		}

		Collections.sort(listRes);
		FileWriter fstream1 = new FileWriter(kid + ".txt",false);
        BufferedWriter bwKW = new BufferedWriter(fstream1);
		
		Vector<Result> list1 = new Vector<Result>();
		Vector<Result> list2 = new Vector<Result>();
		if(listRes.size() > 50)
		{
			Result r50 = listRes.elementAt(49);
			for(int i = 0; i < listRes.size();i++)
			{
				Result r = listRes.elementAt(i); 
				if(r.score > r50.score)
					list1.add(r);
				else if(r.score == r50.score)
					list2.add(r);
				else break;
			}
			Collections.shuffle(list2, new Random(2015));
			int addMore = 50 - list1.size();
			for(int i = 0; i < list2.size();i++)
			{
				list1.add(list2.elementAt(i));
				if(i >= addMore) break;
			}
		}
		else list1 = listRes;
		
		int numSamp = 0;
		for(int i = 0; i < list1.size();i++)
		{
			bwKW.write(kid + " ");
			Result r = list1.elementAt(i);
			bwKW.write(r.result + " " + r.score);
			bwKW.newLine();
			if(r.score <= 0.7 || i >= 49) break;
			numSamp++;
		}
		bwKW.close();
		return (strReturn + numSamp + "," + numSamp);
		
		
	}

	public static void getSortedList(Vector<WordItem> secondList, Vector<WordItem> result, Vector<Double> listScore, WordItem first, int numSelect, HashMap<String, String> utt2AudioMap, BufferedWriter bw2) throws Exception
	{
		String prevAudio = utt2AudioMap.get(first.segment); 
		for(int i2 = 0; i2 < secondList.size();i2++)
		{
			WordItem second = secondList.elementAt(i2);
			String secondAudio = utt2AudioMap.get(second.segment);
			second.score = 1;
			if(!second.segment.equals(first.segment) || Math.abs(Double.parseDouble(second.start) - Double.parseDouble(first.start) - Double.parseDouble(first.end)) > 0.05)
				second.score *= 0.97;
			if(!secondAudio.equals(prevAudio))
				second.score *= 0.95;
			if(!second.spk.equals(first.spk))
				second.score *= 0.93;
			if(!second.leftContext.equals(first.rightContext))
				second.score *= 0.85;
			if(!second.gender.equals(first.gender))
				second.score *= 0.7;
		}
		Collections.sort(secondList);
		//int numSelect = 100;
		if(secondList.size() < numSelect) numSelect = secondList.size();
		//bw2.write("Previous sample:" + first.id + " " + first.start + " " + first.end + " " + first.segment + " " + first.spk + " " + first.gender + " " + utt2AudioMap.get(first.segment));
		//bw2.newLine();
		//bw2.write("List of potential next sample:");
		//bw2.newLine();
		for(int i = 0; i < numSelect; i++)
		{
			WordItem second = secondList.elementAt(i);
			result.add(second);
			listScore.add(second.score);	
			//bw2.write(first.id + " " + first.start + " " + first.end + " " + first.segment + " " + first.spk + " " + first.gender + " " + utt2AudioMap.get(first.segment));
			//bw2.newLine();
		}	

	}
	
	public static void GeneratePermutationsV1(Vector<Vector<WordItem>> Lists, Vector<Result> result, HashMap<String, String> utt2AudioMap, BufferedWriter bw2)
	{
		
		Vector<WordItem> firstList = Lists.elementAt(0);
		for(int i1 = 0; i1 < firstList.size(); i1++)
		{
			WordItem first = firstList.elementAt(i1);
			double scoreFirst = 1.0;
			String contentFirst = first.segment + "," + first.start + "," + first.end;
			result.add(new Result(contentFirst, scoreFirst));
		}
		System.out.println("This keyword has " + result.size() + " raw results");
	}
	
	public static void GeneratePermutationsV2(Vector<Vector<WordItem>> Lists, Vector<Result> result, HashMap<String, String> utt2AudioMap, BufferedWriter bw2) throws Exception
	{
		long totalPrint = 0;
		Vector<WordItem> firstList = Lists.elementAt(0);
		for(int i1 = 0; i1 < firstList.size(); i1++)
		{
			WordItem first = firstList.elementAt(i1);
			double scoreFirst = 1.0;
			//String contentFirst = first.segment + "," + first.start + "," + first.end;
			Vector<WordItem> secondList = Lists.elementAt(1);
			Vector<WordItem> secondListSelected = new Vector<WordItem>();
			Vector<Double>secondListScore = new Vector<Double>();
			getSortedList(secondList, secondListSelected, secondListScore, first, 100, utt2AudioMap, bw2);
		
			for(int i2 = 0; i2 < secondListSelected.size(); i2++)
			{
				WordItem second = secondListSelected.elementAt(i2);
				double score = secondListScore.elementAt(i2);
				String contentSecond = second.segment + "," + second.start + "," + second.end;
				String r = first.segment + "," + first.start + "," + first.end + ";" + second.segment + "," + second.start + "," + second.end;
				score = scoreFirst * score;
				result.add(new Result(r, round(score,5)));
				r = first.segment + "," + first.start + "," + first.end +"," + first.gender + "," + first.spk + "," + first.rightContext + "," + utt2AudioMap.get(first.segment) + ";" + second.segment + "," + second.start + "," + second.end + "," + second.gender + "," + second.spk + "," + second.leftContext + "," +utt2AudioMap.get(second.segment) + "  " + score;
				if(totalPrint < 10)
				{
					bw2.write(r);
					bw2.newLine();
				}
				totalPrint++;
			}	
		}
	}
	
	public static void GeneratePermutationsV3(Vector<Vector<WordItem>> Lists, Vector<Result> result, HashMap<String, String> utt2AudioMap, BufferedWriter bw2) throws Exception
	{
		long totalPrint = 0;
		Vector<WordItem> firstList = Lists.elementAt(0);
		for(int i1 = 0; i1 < firstList.size(); i1++)
		{
			WordItem first = firstList.elementAt(i1);	
			
			Vector<WordItem> secondList = Lists.elementAt(1);
			Vector<WordItem> secondListSelected = new Vector<WordItem>();
			Vector<Double>secondListScore = new Vector<Double>();
			getSortedList(secondList, secondListSelected, secondListScore, first, 40, utt2AudioMap, bw2);
		
			for(int i2 = 0; i2 < secondListSelected.size(); i2++)
			{
				WordItem second = secondListSelected.elementAt(i2);
				double scoreSecond = secondListScore.elementAt(i2);
				
				
				Vector<WordItem> thirdList = Lists.elementAt(2);
				Vector<WordItem> thirdListSelected = new Vector<WordItem>();
				Vector<Double>thirdListScore = new Vector<Double>();
				getSortedList(thirdList, thirdListSelected, thirdListScore, second, 40, utt2AudioMap, bw2);
				for(int i3 = 0; i3 < thirdListSelected.size(); i3++)
				{
					WordItem third = thirdListSelected.elementAt(i3);
					double scoreThird = thirdListScore.elementAt(i3);
					String r = first.segment + "," + first.start + "," + first.end + ";" + second.segment + "," + second.start + "," + second.end + ";" + third.segment + "," + third.start + "," + third.end;
					double score = 1.0 * secondListScore.elementAt(i2) * thirdListScore.elementAt(i3);
					result.add(new Result(r, round(score,5)));
					r = first.segment + "," + first.start + "," + first.end  +"," + first.gender + "," + first.spk + "," + first.rightContext + "," + utt2AudioMap.get(first.segment) + ";" + second.segment + "," + second.start + "," + second.end + "," + second.gender + "," + second.spk + "," + second.leftContext + "," + second.rightContext + "," +utt2AudioMap.get(second.segment) + ";" + third.segment + "," + third.start + "," + third.end + "," + third.gender + "," + third.spk + "," + third.leftContext + "," +utt2AudioMap.get(third.segment) + "  " + score;
					if(totalPrint < 10)
					{
						bw2.write(r);
						bw2.newLine();
					}
					totalPrint++;
				}	
				
			}	
		}
	}
	
	public static void GeneratePermutationsV4(Vector<Vector<WordItem>> Lists, Vector<Result> result, HashMap<String, String> utt2AudioMap, BufferedWriter bw2) throws Exception
	{
		
		Vector<WordItem> firstList = Lists.elementAt(0);
		for(int i1 = 0; i1 < firstList.size(); i1++)
		{
			WordItem first = firstList.elementAt(i1);
			
			Vector<WordItem> secondList = Lists.elementAt(1);
			Vector<WordItem> secondListSelected = new Vector<WordItem>();
			Vector<Double>secondListScore = new Vector<Double>();
			getSortedList(secondList, secondListSelected, secondListScore, first, 17, utt2AudioMap, bw2);
		
			for(int i2 = 0; i2 < secondListSelected.size(); i2++)
			{
				WordItem second = secondListSelected.elementAt(i2);
				
				Vector<WordItem> thirdList = Lists.elementAt(2);
				Vector<WordItem> thirdListSelected = new Vector<WordItem>();
				Vector<Double>thirdListScore = new Vector<Double>();
				getSortedList(thirdList, thirdListSelected, thirdListScore, second, 17, utt2AudioMap, bw2);
				for(int i3 = 0; i3 < thirdListSelected.size(); i3++)
				{
					WordItem third = thirdListSelected.elementAt(i3);
					
					Vector<WordItem> fourList = Lists.elementAt(3);
					Vector<WordItem> fourListSelected = new Vector<WordItem>();
					Vector<Double>fourListScore = new Vector<Double>();
					getSortedList(fourList, fourListSelected, fourListScore, third, 17, utt2AudioMap, bw2);
					for(int i4 = 0; i4 < fourListSelected.size(); i4++)
					{
						WordItem four = fourListSelected.elementAt(i4);
						
						String r = first.segment + "," + first.start + "," + first.end + ";" + second.segment + "," + second.start + "," + second.end + ";" + third.segment + "," + third.start + "," + third.end + ";" + four.segment + "," + four.start + "," + four.end;
						double score = 1.0 * secondListScore.elementAt(i2) * thirdListScore.elementAt(i3) * fourListScore.elementAt(i4);
						result.add(new Result(r, round(score,5)));
				
					}
				}	
				
			}	
		}
	}
	
	public static void GeneratePermutationsV5(Vector<Vector<WordItem>> Lists, Vector<Result> result, HashMap<String, String> utt2AudioMap, BufferedWriter bw2) throws Exception
	{
		
		Vector<WordItem> firstList = Lists.elementAt(0);
		for(int i1 = 0; i1 < firstList.size(); i1++)
		{
			WordItem first = firstList.elementAt(i1);
			
			Vector<WordItem> secondList = Lists.elementAt(1);
			Vector<WordItem> secondListSelected = new Vector<WordItem>();
			Vector<Double>secondListScore = new Vector<Double>();
			getSortedList(secondList, secondListSelected, secondListScore, first, 8, utt2AudioMap, bw2);
		
			for(int i2 = 0; i2 < secondListSelected.size(); i2++)
			{
				WordItem second = secondListSelected.elementAt(i2);
				
				Vector<WordItem> thirdList = Lists.elementAt(2);
				Vector<WordItem> thirdListSelected = new Vector<WordItem>();
				Vector<Double>thirdListScore = new Vector<Double>();
				getSortedList(thirdList, thirdListSelected, thirdListScore, second, 8, utt2AudioMap, bw2);
				for(int i3 = 0; i3 < thirdListSelected.size(); i3++)
				{
					WordItem third = thirdListSelected.elementAt(i3);
					
					Vector<WordItem> fourList = Lists.elementAt(3);
					Vector<WordItem> fourListSelected = new Vector<WordItem>();
					Vector<Double> fourListScore = new Vector<Double>();
					getSortedList(fourList, fourListSelected, fourListScore, third, 8, utt2AudioMap, bw2);
					for(int i4 = 0; i4 < fourListSelected.size(); i4++)
					{
						WordItem four = fourListSelected.elementAt(i4);
						
						Vector<WordItem> fiveList = Lists.elementAt(4);
						Vector<WordItem> fiveListSelected = new Vector<WordItem>();
						Vector<Double> fiveListScore = new Vector<Double>();
						getSortedList(fiveList, fiveListSelected, fiveListScore, four, 8, utt2AudioMap, bw2);
						for(int i5 = 0; i5 < fiveListSelected.size(); i5++)
						{
							WordItem five = fiveListSelected.elementAt(i5);
						
							String r = first.segment + "," + first.start + "," + first.end + ";" + second.segment + "," + second.start + "," + second.end + ";" + third.segment + "," + third.start + "," + third.end + ";" + four.segment + "," + four.start + "," + four.end + ";" + five.segment + "," + five.start + "," + five.end;
							double score = 1.0 * secondListScore.elementAt(i2) * thirdListScore.elementAt(i3) * fourListScore.elementAt(i4) * fiveListScore.elementAt(i5) ;
							result.add(new Result(r, round(score,5)));
				
						}
				
					}
				}	
				
			}	
		}
	}
	
	public static void GeneratePermutationsV6(Vector<Vector<WordItem>> Lists, Vector<Result> result, HashMap<String, String> utt2AudioMap, BufferedWriter bw2) throws Exception
	{
		
		Vector<WordItem> firstList = Lists.elementAt(0);
		for(int i1 = 0; i1 < firstList.size(); i1++)
		{
			WordItem first = firstList.elementAt(i1);
			
			Vector<WordItem> secondList = Lists.elementAt(1);
			Vector<WordItem> secondListSelected = new Vector<WordItem>();
			Vector<Double>secondListScore = new Vector<Double>();
			getSortedList(secondList, secondListSelected, secondListScore, first, 5, utt2AudioMap, bw2);
		
			for(int i2 = 0; i2 < secondListSelected.size(); i2++)
			{
				WordItem second = secondListSelected.elementAt(i2);
				
				Vector<WordItem> thirdList = Lists.elementAt(2);
				Vector<WordItem> thirdListSelected = new Vector<WordItem>();
				Vector<Double>thirdListScore = new Vector<Double>();
				getSortedList(thirdList, thirdListSelected, thirdListScore, second, 5, utt2AudioMap, bw2);
				for(int i3 = 0; i3 < thirdListSelected.size(); i3++)
				{
					WordItem third = thirdListSelected.elementAt(i3);
					
					Vector<WordItem> fourList = Lists.elementAt(3);
					Vector<WordItem> fourListSelected = new Vector<WordItem>();
					Vector<Double> fourListScore = new Vector<Double>();
					getSortedList(fourList, fourListSelected, fourListScore, third, 5, utt2AudioMap, bw2);
					for(int i4 = 0; i4 < fourListSelected.size(); i4++)
					{
						WordItem four = fourListSelected.elementAt(i4);
						
						Vector<WordItem> fiveList = Lists.elementAt(4);
						Vector<WordItem> fiveListSelected = new Vector<WordItem>();
						Vector<Double> fiveListScore = new Vector<Double>();
						getSortedList(fiveList, fiveListSelected, fiveListScore, four, 5, utt2AudioMap, bw2);
						for(int i5 = 0; i5 < fiveListSelected.size(); i5++)
						{
							WordItem five = fiveListSelected.elementAt(i5);
							Vector<WordItem> sixList = Lists.elementAt(5);
							Vector<WordItem> sixListSelected = new Vector<WordItem>();
							Vector<Double> sixListScore = new Vector<Double>();
							getSortedList(sixList, sixListSelected, sixListScore, five, 5, utt2AudioMap, bw2);
							for(int i6 = 0; i6 < sixListSelected.size(); i6++)
							{
								WordItem six = sixListSelected.elementAt(i6);
						
								String r = first.segment + "," + first.start + "," + first.end + ";" + second.segment + "," + second.start + "," + second.end + ";" + third.segment + "," + third.start + "," + third.end + ";" + four.segment + "," + four.start + "," + four.end + ";" + five.segment + "," + five.start + "," + five.end + ";" + six.segment + "," + six.start + "," + six.end;
								double score = 1.0 * secondListScore.elementAt(i2) * thirdListScore.elementAt(i3) * fourListScore.elementAt(i4) * fiveListScore.elementAt(i5) * sixListScore.elementAt(i6) ;
								result.add(new Result(r, round(score,5)));
				
							}
						}
				
					}
				}	
				
			}	
		}
	}
	
	
	public static void loadCTM_subword(String ctm, HashMap<String, Segment > id2Seg, HashMap<String, String> subId2Label)
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
        	while ((strLine = br.readLine()) != null)   {
				StringTokenizer token = new StringTokenizer(strLine);
				String segId = token.nextToken();
				token.nextToken();
				String start = token.nextToken();
				String end = token.nextToken();
				String word = token.nextToken();
				
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
        	}
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void loadCTM(String ctm, HashMap<String, Segment > id2Seg)
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
        	while ((strLine = br.readLine()) != null)   {
				StringTokenizer token = new StringTokenizer(strLine);
				String segId = token.nextToken();
				token.nextToken();
				String start = token.nextToken();
				String end = token.nextToken();
				String word = token.nextToken();
				
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
		String keywordFile_subword = args[1];
		String outFile = args[2];
		String log = args[3];
		String audio2Gender = args[4];
		String segmentFile = args[5];
		String utt2spk = args[6];
		processKWFile(ctm_subword, keywordFile_subword, outFile, log, audio2Gender, segmentFile, utt2spk);
		
		
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
	public Segment()
	{
		trans = "";
		list = new Vector<WordItem>();
		segment = "";
	}
}

