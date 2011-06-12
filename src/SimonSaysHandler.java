/*
    This file is part of thriftwork
    Copyright (C) 2011 Toby Thain, toby@telegraphics.com.au

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
import org.apache.thrift.TException;

import java.util.*;

public class SimonSaysHandler implements SimonSays.Iface {
	
	/* per-client game state */
	protected String clientKey;
	protected int turns = 0;
	protected boolean winState = false;
	protected boolean inTurn;
	protected int winColours;
	protected int coloursReturned;
	protected ArrayList<Color> turnColours;
	
	protected static HashMap<String,SimonSaysHandler> games = new HashMap<String,SimonSaysHandler>();

	protected void checkRegistered() throws TException {
		if(clientKey == null)
			throw new TException("client did not register");
	}
	
    public boolean registerClient(String email) throws TException {
    	if(games.containsKey(email)){
    		System.err.println("cannot register "+email+": already registered");
    		return false;
    	}
    	clientKey = email;
    	games.put(email, this);
    	System.out.println("registered client: "+email+" handler obj: "+this);

    	winColours = 5 + (int)Math.floor(3*Math.random());
    	resetTurn();
    	System.out.println("must replay "+winColours+" colours to win");

        return true;
    }
    
    protected void resetTurn(){
    	turnColours = new ArrayList<Color>();
    	inTurn = false;
    }

    public List<Color> startTurn() throws TException {
    	checkRegistered();

    	if(inTurn)
    		resetTurn(); //throw new TException("startTurn without endTurn");

		Color c = Color.findByValue((int)Math.ceil(Math.random()*Color.values().length));
		turnColours.add(c);

		System.out.print("turn #" + turns + ", sending colours:");
		for(Color col : turnColours)
    		System.out.print(" " + col);
    	System.out.println();

    	inTurn = true;
    	coloursReturned = 0;
        return turnColours;
    }

    public boolean chooseColor(Color colorChosen) throws TException {
    	checkRegistered();

    	if(!inTurn)
    		throw new TException("chooseColor without startTurn");

    	// are we still expecting a colour?
    	if(inTurn && coloursReturned < turnColours.size()){
    		// did client give correct colour in sequence?
	        if(colorChosen == turnColours.get(coloursReturned)){
	        	++coloursReturned;
	        	System.out.println("  client said correct colour: " + colorChosen);
	        	// is this turn finished?
	        	if(coloursReturned == turnColours.size()){
	        		++turns;
	        		System.out.println("turn complete");
	        	}
	        	return true;
	        }else{
	        	System.out.println("  client said wrong colour: " + colorChosen);
	        }
    	}
      	resetTurn();
        return false;
    }

    public boolean endTurn() throws TException {
    	checkRegistered();

    	if(!inTurn)
    		resetTurn(); // throw new TException("endTurn without startTurn");

    	inTurn = false;
    	return winState = coloursReturned == winColours;
    }

    public String winGame() throws TException {
    	checkRegistered();

    	games.remove(clientKey);
    	System.out.println("de-registered client: "+clientKey);
    	clientKey = null;
    	return winState ? "you won a game" : "you didn't win";
    }
}

