package smashed;


// ***********************************************************************
   
public class NumberWorkSpace implements  WorkSpace {
/*@(Number)*/
 
	int vertexCounter;
/*@(Number)*/
 

	public NumberWorkSpace() {
        vertexCounter = 0;
    }
/*@(Number)*/
 

	public void preVisitAction( Vertex v )
    {
        // This assigns the values on the way in
        if ( v.visited!=true )
            v.VertexNumber = vertexCounter++;
    }


	//@Override
	public void checkNeighborAction(Vertex vsource, Vertex vtarget) {
		// TODO Auto-generated method stub
		
	}


	//@Override
	public void init_vertex(Vertex v) {
		// TODO Auto-generated method stub
		
	}


	//@Override
	public void nextRegionAction(Vertex v) {
		// TODO Auto-generated method stub
		
	}


	//@Override
	public void postVisitAction(Vertex v) {
		// TODO Auto-generated method stub
		
	}
}
