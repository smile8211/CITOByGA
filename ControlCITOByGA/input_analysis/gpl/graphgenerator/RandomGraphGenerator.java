package graphgenerator;

import java.io.IOException;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import runspl.JavaUtility;
import smashed.TreeGenerator;



public class RandomGraphGenerator {
	public static void main(String args[])
	{
		Graph graph = new SingleGraph("Random");
		Generator gen = new RandomGenerator(10, true, true);
		gen.addSink(graph);
		gen.begin();
		for(int i=0; i<2000; i++)
			gen.nextEvents();
		gen.end();

		// Write to GPL format
		String gplStr = graph.getNodeCount() + " " + graph.getEdgeCount() + " 99 99 99\n";
		for(Edge edge: graph.getEachEdge())
			gplStr += edge.getSourceNode().getIndex() + " " + edge.getTargetNode().getIndex() + "\n"; 
		for(int i = 0; i < graph.getEdgeCount(); i++)
			gplStr += "10\n";

		JavaUtility.INSTANCE.writeToFile(TreeGenerator.FILENAME, gplStr);

		graph.display();
	}
}
