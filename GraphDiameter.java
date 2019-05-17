package webcrawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.print.attribute.standard.Finishings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GraphDiameter {
    public static HashSet<String> visitedLinks;
    public static HashMap<String, Node> nodeHashMap;
    public static Queue<Node> queue;
    public static String startPage = "https://cs.txstate.edu/";

    public GraphDiameter(){
        nodeHashMap = new HashMap<String, Node>();
        visitedLinks = new HashSet<String>();
        queue = new LinkedList<Node>();
        startScraping();
    }
    
    public static void main(String[] args) throws Exception{

    	long startTime = System.currentTimeMillis();
    	
        System.setProperty ("https.protocols", "TLSv1.1,TLSv1.2");
        GraphDiameter gd = new GraphDiameter();
        //generate Adjacency matrix
        int[][] graph = generateAdjacencyMatrix();
        
        //printGraph(graph);
        
        //calculate Diameter from Node 0;
        //System.out.println("Diameter from Node 0");
        //diameterForEachNode(graph, 43);
        
        calculateDiameter(graph);
        
        printOutlinksAndInlinksForGraph();
        
        System.out.println("Time taken to run is : " + (System.currentTimeMillis()-startTime) / 1000 + " seconds");
        System.out.println();
    }

	private static void printOutlinksAndInlinksForGraph() throws IOException {
		System.out.println("Printing outlinks and inlinks for each node");
		
	    BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
	    
	    for(Map.Entry<String, Node> entry : nodeHashMap.entrySet()){
    		Node curr = entry.getValue();
    		writer.write("Node URL : "+curr.url);
    		writer.newLine();
    		writer.write("Node Id : "+curr.id);
    		writer.newLine();
    		
    		writer.write("Outlinks : ");
    		writer.newLine();
    		HashSet<Node> outgoing = curr.outgoing;
    		
    		for(Node n  :  outgoing){
    			writer.write("\t"+n.url);
        		writer.newLine();
    		}
    		
    		writer.write("Inlinks : ");
    		writer.newLine();
    		HashSet<Node> inlinks = curr.incoming;
    		
    		for(Node x  :  inlinks){
    			writer.write("\t"+x.url);
        		writer.newLine();
    		}
    		
    		writer.newLine();
    		writer.newLine();
    		writer.newLine();
    	}
	    writer.newLine();
	     
	    writer.close();
	    
	    System.out.println("Outlinks and Inlinks are generated in ***output.txt*** file");
		
	}


	private static int calculateDiameter(int[][] graph) {
		int graphLength = graph.length;
		Result result = new Result();
		Result temp;
		
		for( int i =0 ; i < graphLength ; i++){
			temp = diameterForEachNode(graph, i);
			if(temp.dist > result.dist ){
				result = temp;
			}
		}
		
		System.out.println("");
		System.out.println("");
		System.out.println("*********************************************        ");
		System.out.println("Diameter of the graph is : "+result.dist);
		Node source = findNode(result.sourceNode);
		Node dest = findNode(result.destNode);
		System.out.println("Source : "+source.url  );
		System.out.println("Destination : "+dest.url);
		
		return result.dist;
	}
	
	public static Node findNode(int n){
		for(Map.Entry<String, Node> entry : nodeHashMap.entrySet()){
    		Node curr = entry.getValue();
    		
    		if(curr.id == n)
    			return curr;
    		
    	}
		return null;
	}

	public void startScraping(){
        Node startNode = generateNewNode(startPage);
        queue.add(startNode);

        // checking the top
        while (!queue.isEmpty()){
            // removing the top
            Node head = queue.poll();
            String parentURL = head.url;

            if(visitedLinks.contains(parentURL)){
                continue;
            }

            //System.out.println("Adding URL to Visited List "+parentURL);
            visitedLinks.add(parentURL);

            System.out.println("Crawling  "+parentURL);

            nodeHashMap.put(parentURL,head);

            //scrap this URL
            crawlPage(parentURL,head);
        }

        //Printing HashMap and Visited Node links
        //System.out.println("--- completed --");
        //System.out.println(nodeHashMap.size());
        //System.out.println("--- map --");
        //System.out.println( nodeHashMap.keySet());
        //System.out.println("--- map --");
        //System.out.println( visitedLinks);
        //System.out.println("--- Node --");
        //System.out.println(visitedLinks.size());
        
        /*
        System.out.println("Print Node Values");
        
        for(Map.Entry<String,Node> entry : nodeHashMap.entrySet()){
        	int nodeVal = entry.getValue().id;
        	System.out.println(nodeVal);
        }
        */
    }


    public void crawlPage(String parentUrl, Node parentNode){
        try {
            Document document = Jsoup.connect(parentUrl).timeout(8000).get();
            Elements linksOnPage;
            
            if(!parentUrl.equalsIgnoreCase(startPage)){
        		document.selectFirst("header.header").remove();
        		document.selectFirst("nav.main-nav").remove();
        		document.selectFirst("footer.footer").remove();
        	}
            
        	linksOnPage = document.select("a[href]");

            for(Element page: linksOnPage){
                String child = page.attr("abs:href");
                String formatedChild = formatURL(child);
                if(isValidUrl(formatedChild)){
                    Node childNode;
                    if(!nodeHashMap.containsKey(formatedChild)){
                        childNode = generateNewNode(formatedChild);
                        nodeHashMap.put(formatedChild,childNode);
                    }else{
                        childNode = nodeHashMap.get(formatedChild);
                    }

                    //AddOutgoingfromparent
                    parentNode.addOutgoingEdge(childNode);

                    //AddIncomingToChild
                    childNode.addIncomingEdge(parentNode);

                    //addchildNode to Queue
                    queue.add(childNode);
                }
            }
        } catch (Exception e){
            System.err.println("For '" + parentUrl + "': " + e.getMessage());
        }
    }

    private Node generateNewNode(String link){
        Node n1 = new Node(formatURL(link));
        return n1;
    }

    private String formatURL(String pageURL) 
    {
        pageURL = pageURL.toLowerCase().split("\\?")[0];
        pageURL = pageURL.toLowerCase().split("\\#")[0];
        
        if(pageURL.endsWith("/#"))
            return pageURL.substring(0, pageURL.length()-2);

        if(pageURL.endsWith("#"))
            return pageURL.substring(0, pageURL.length()-1);

        pageURL = pageURL.split("\\#")[0];

        return pageURL;
    }

    private boolean isValidUrl(String page) {

        /**
         * remove https://cs.txstate.edu#events-carousel
         * remove https://cs.txstate.edu#
         */
        //if(!(page.toLowerCase().endsWith("/")))
          //  return false;

        if(
              //page.toLowerCase().contains("profiles") ||
              page.toLowerCase().contains("course_detail") ||
              //page.toLowerCase().contains("news_detail") ||
              //page.toLowerCase().contains("login") ||
              //page.toLowerCase().contains("semester_list") ||
              page.toLowerCase().contains(".pdf") ||
              page.toLowerCase().contains(".doc") ||
              page.toLowerCase().contains(".docx") ||
              page.toLowerCase().contains(".cpp")||
              page.toLowerCase().contains(".txt")
          )
            return false;

        if(page.toLowerCase().startsWith("https://cs.txstate.edu"))
            return true;

        return false;
    }
    
    public static int[][] generateAdjacencyMatrix(){
    	int size = nodeHashMap.size();
    	
    	int[][] matrix = new int[size][size];
    	
    	for(Map.Entry<String, Node> entry : nodeHashMap.entrySet()){
    		String key = entry.getKey();
    		Node curr = entry.getValue();
    		
    		HashSet<Node> outgoing = curr.outgoing;
    		
    		for(Node n  :  outgoing){
    			matrix[curr.id][n.id] = 1;
    		}
    		
    	}
    	return matrix;
    }
    
    public static void printGraph(int[][] matrix){
    	for(int i = 0 ; i < matrix.length ; i ++){
    		for(int j = 0 ; j < matrix[0].length ; j++){
    			System.out.print(matrix[i][j]+ ", ");
    		}
    		System.out.println();
    	}
    }
    
    public static Result diameterForEachNode(int graph[][], int src)
    {
    	int graphLength = graph.length;
        int dist[] = new int[graphLength]; // The output array. dist[i] will hold
 
        Boolean sptSet[] = new Boolean[graphLength];
 
        // Initialize all distances as INFINITE and stpSet[] as false
        for (int i = 0; i < graphLength; i++)
        {
            dist[i] = Integer.MAX_VALUE;
            sptSet[i] = false;
        }
 
        dist[src] = 0;
 
        // Find shortest path for all vertices
        for (int count = 0; count < graphLength-1; count++)
        {
            int u = minDistance(dist, sptSet, graphLength);
            sptSet[u] = true;

            for (int v = 0; v < graphLength ; v++)
            {
            	if (!sptSet[v] && graph[u][v]!=0 &&
                        dist[u] != Integer.MAX_VALUE &&
                        dist[u]+graph[u][v] < dist[v])
                    dist[v] = dist[u] + graph[u][v];
            }  
        }

        Result result = printSolution(dist, graphLength, src);
        
        return result;
    }
    
    public static Result printSolution(int dist[], int graphLength, int src)
    {
    	int max = -1;
    	Result result = new Result();
    	
        //System.out.println("Vertex   Distance from Source");
        for (int i = 0; i < graphLength; i++){
        	int d = dist[i];
        	//System.out.println(i+" tt "+dist[i]);
        	if(d != Integer.MAX_VALUE && (d > max) ){
        		max = d;
        		result.dist = max;
        		result.sourceNode = src;
        		result.destNode = i;
        	}
        }
        
        return result;
    }
    
    public static int minDistance(int dist[], Boolean sptSet[], int graphLength)
    {
        // Initialize min value
        int min = Integer.MAX_VALUE, min_index=-1;
 
        for (int v = 0; v < graphLength; v++)
            if (sptSet[v] == false && dist[v] <= min)
            {
                min = dist[v];
                min_index = v;
            }
 
        return min_index;
    }
    
    
}
