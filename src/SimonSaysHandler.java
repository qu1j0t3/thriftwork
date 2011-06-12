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
	protected int gameTurns;
	protected int coloursReturned;
	protected ArrayList<Color> turnColours;
	
	protected static HashMap<String,SimonSaysHandler> games = new HashMap<String,SimonSaysHandler>();

    public boolean registerClient(String email) throws org.apache.thrift.TException {
    	if(games.containsKey(email)){
    		System.err.println("cannot register "+email+": already registered");
    		return false;
    	}
    	clientKey = email;
    	gameTurns = 3 + (int)Math.floor(4*Math.random());
    	resetTurn();
    	games.put(email, this);

    	System.out.println("registered client: "+email+" handler obj: "+this);
    	System.out.println("game will have "+gameTurns+" turns");
        return true;
    }
    
    protected void resetTurn(){
    	coloursReturned = 0;
    }

    public List<Color> startTurn() throws org.apache.thrift.TException {
    	if(turnColours == null)
	    	turnColours = new ArrayList<Color>();

		Color c = Color.findByValue((int)Math.ceil(Math.random()*Color.values().length));
		turnColours.add(c);

		System.out.print("turn #" + turns + ", sending colours:");
		for(Color col : turnColours){
    		System.out.print(" "+col);
    	}
    	System.out.println();
        return turnColours;
    }

    public boolean chooseColor(Color colorChosen) throws org.apache.thrift.TException {
    	// are we still expecting a colour?
    	if(coloursReturned < turnColours.size()){
    		// did client give correct colour in sequence?
	        if(colorChosen == turnColours.get(coloursReturned)){
	        	++coloursReturned;
	        	System.out.println("  client responded with correct colour: "+colorChosen);
	        	// is this turn finished?
	        	if(coloursReturned == turnColours.size()){
	        		++turns;
	        		resetTurn();
	        		System.out.println("turn complete");
	        	}
	        	return true;
	        }
    	}
      	resetTurn();
        return false;
    }

    public boolean endTurn() throws org.apache.thrift.TException {
    	return winState = turns == gameTurns;
    }

    public String winGame() throws org.apache.thrift.TException {
    	if(winState){
	    	games.remove(clientKey);
	    	System.out.println("de-registered client: "+clientKey);
	        return "you won a game";
    	}
    	throw new TException("winGame called but game hasn't been won");
    }
}

