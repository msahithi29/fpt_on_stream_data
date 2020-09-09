import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
class P2
{
    FPTree fpTree = new FPTree();
    Lock lock= new ReentrantLock();
    public static void main(String[] args) throws IOException
    {
        P2 p2 = new P2();
        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	System.out.println("Enter socket - host or ip and port separated by comma:");
        while (true) 
        {
	    s=in.readLine();
	    if(s==null) // if no input, ignore
	    {
		continue;
	    }
            String s1[] = s.split(",");
            if(s1.length>1) {
		//socket input
                SocketReader socketReader = new SocketReader(s1[0].trim(), s1[1].trim(),p2.fpTree,p2.lock);
                Thread t = new Thread(socketReader);
                t.start(); // creating and starting thread
            }
            else
            {  // support input
		int support=1;
                System.out.println("Input Threshold:"+ s);
                try
                {
                    p2.lock.lock(); //acquire lock
		    try
		    {
			support = Integer.parseInt(s.trim());
			if(support<=0) //handling negative number
			{
				System.out.println("Enter positive number");
				continue;
			}
			p2.fpTree.print_frequent_patterns(support);	
		    }
		    catch(NumberFormatException ex) //handling strings in place of integers
		    { 
			System.out.println("Given support is not valid. Please enter positive number");
		//	return;	
		    }
		    //p2.fpTree.print_frequent_patterns(support);
                }
                finally {
                    p2.lock.unlock(); // relaease lock after display
                }
            }
        }
    }
}
class FPTree
{
    HashMap<String,Node> fptrees = new HashMap<>(); //storing header and trees
    synchronized void updateTree(List<String> mfrarray) //updating tree for each mfr
    {
        while(mfrarray.size()!=0)
        {
            updateNode(mfrarray);
            mfrarray.remove(0);
        }
    }
    void updateNode(List<String> mfrarray) //updating nodes in the tree
    {
        Node root = null;
        if(fptrees.containsKey(mfrarray.get(0)))
        {
            root = fptrees.get(mfrarray.get(0));
            if(mfrarray.size()==1)
            {
                root.count=root.count+1;
            }
            for(int i=1;i<mfrarray.size();i++)
            {
               if(root.nodes.containsKey(mfrarray.get(i)))
                {
                    root = root.nodes.get(mfrarray.get(i));
                    if(i==mfrarray.size()-1)
                    {
                        root.count=root.count+1;
                    }
                }
                else
                {
                    Node child = new Node(mfrarray.get(i),1);
                    root.nodes.put(mfrarray.get(i),child);
                }
            }
        }
        else
        {
            root = new Node(mfrarray.get(0),1);
            fptrees.put(mfrarray.get(0),root);
        }
    }
    void print_frequent_patterns(int support) //display path traversals frequent
    {
        System.out.println("**********************");
	System.out.println("Frequent path traversal patterns");
        ArrayList<ArrayList<Node>> temp2dArray;
        ArrayList<Node> temp1dArray = null;
        temp2dArray = new ArrayList<>();
        for(Node node: fptrees.values())
        {
            if(node.count>= support) {
                temp1dArray = new ArrayList<>();
                temp1dArray.add(node);
                temp2dArray.add(temp1dArray);
            }
        }
        int len=1;
        while(temp2dArray.size()>0)
        {
            System.out.println("\nWith support " + support + ", the frequent-path traversal patterns with length " + len + " are:");
            int serial_no=1;
	    for(ArrayList<Node> node2:temp2dArray)
            {
		System.out.println("");
		System.out.print(serial_no + ". ");
                for(Node node3: node2)
                {
                    System.out.print("\"" + node3.url.trim() + "\"" + "  ");
                }
		serial_no = serial_no +1;
                System.out.println("");
            }
           ArrayList<ArrayList<Node>> temp3dArray = new ArrayList<>();
            for(ArrayList<Node> list: temp2dArray)
            {
                Node endnode = list.get(list.size()-1);
                for(Node node: endnode.nodes.values())
                {
                    if(node.count>=support)
                    {
                        ArrayList<Node> temp = new ArrayList<>();
                        temp.addAll(list);
                        temp.add(node);
                        temp3dArray.add(temp);
                    }
                }
            }
            temp2dArray = temp3dArray;
            len = len+1;
       }
       System.out.println("*************************");  
    }
}
class Node //data structure of tree contains url, count and list of nodes
{
    int count=0;
    String url;
    HashMap<String,Node> nodes;
    Node(String url,int count)
    {
        this.url = url;
        this.count = count;
        this.nodes = new HashMap<>();
    }
}
class SocketReader implements Runnable // reading sockets 
{
    Socket socket;
    String ip;
    String port;
    FPTree fpTree;
    Lock lock;
    Map<Integer,ArrayList<String>> wcsMap = new HashMap<>();
    ArrayList<List<String>> mfrSequence = new ArrayList<>();
    ArrayList<String> stringArrayList;
    public SocketReader(String s1, String s2, FPTree fpTree, Lock lock)
    {
        this.ip = s1;
        this.port = s2;
        this.fpTree = fpTree;
        this.lock=lock;
    }
    public void read_socket(String s1,String s2,FPTree fpTree,Lock lock) throws IOException, InterruptedException {
        try
	{
	socket = new Socket(s1,Integer.parseInt(s2));
	}
	catch(UnknownHostException e)
	{
		System.out.println("Given socket is not valid. Enter corrrect socket");
		return;
	}
	catch(NumberFormatException e)
	{
		System.out.println("Port number is not correct or doesnt exist");
		return;
	}
	catch(ConnectException e)
	{
		System.out.println("No open connection");
		return;
	}
	catch(Exception e)
	{
		System.out.println("Socket not valid");
		return;
	}
        System.out.println("Reading from socket " + s1 + "," +s2);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        while ((line=reader.readLine())!=null)
        {
            
            String strings[] = line.split(",");
            try
            {
                lock.lock();
            System.out.println(strings[0] + "," + strings[1] );
            }
            finally {
                lock.unlock();
            }
	     
            if (wcsMap.containsKey(Integer.parseInt(strings[0])))
            {
                wcsMap.get(Integer.parseInt(strings[0])).add(strings[1]);
            }
            else
            {
                stringArrayList = new ArrayList<>();
                stringArrayList.add(strings[1]);
                wcsMap.put(Integer.parseInt(strings[0]),stringArrayList);// create web click sequences
            }
	    List<String> mfrArray = new ArrayList<String>();
            HashSet<String> mfrSet = new HashSet<>();
            for (String value : wcsMap.get(Integer.parseInt(strings[0])))
            {
                    if (mfrSet.contains(value))
                    {
                        mfrSequence.add(mfrArray);
                        stringArrayList = new ArrayList<>();
                        wcsMap.put(Integer.parseInt(strings[0]),stringArrayList);
                        wcsMap.get(Integer.parseInt(strings[0])).add(value);
                        mfrSet.clear();
                        mfrArray = new ArrayList<>();
                    }
                    mfrSet.add(value);
                    mfrArray.add(value); // mfr's created
                }
                fpTree.updateTree(mfrArray);
        }
    }
    @Override
    public void run() {
        try {
            read_socket(ip,port,fpTree,lock); //each socket thread calling the read socket method
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

