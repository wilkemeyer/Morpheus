package net.quetzi.morpheus.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.quetzi.morpheus.Morpheus;

import java.util.HashMap;
import java.util.Map.Entry;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.FTBUtilitiesConfig;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesPlayerData;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesUniverseData;

public class WorldSleepState
{
    private int                      dimension;
    private HashMap<String, Boolean> playerStatus;

    public WorldSleepState(int dimension)
    {
        this.dimension = dimension;
        this.playerStatus = new HashMap<>();
    }

    public int getPercentSleeping()
    {
    	int numPlayers = this.playerStatus.size() - this.getIgnoredPlayers();    	
        return (numPlayers > 0) ? (this.getSleepingPlayers() > 0) ? (this.getSleepingPlayers() * 100) / (numPlayers) : 0 : 100;
    }

    private int getIgnoredPlayers()
    {
        int miningPlayers = 0;
        int spawnPlayers = 0;
        int afkPlayers = 0;
        
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(this.dimension);

        Universe universe = null;
        if(Universe.loaded())
        	universe = Universe.get();
        
        for (EntityPlayer player : world.playerEntities)
        {
            if (player.posY < Morpheus.groundLevel)
            {
                miningPlayers++;
            }
            
            if (player.dimension == 0 && FTBUtilitiesUniverseData.isInSpawn(server, new ChunkDimPos(player)) ) {
            	spawnPlayers++;
            }
            
            if(Morpheus.ignoreAfkPlayers) {
            	FTBUtilitiesPlayerData data = FTBUtilitiesPlayerData.get(universe.getPlayer(player));
            	if(data.afkTime >= FTBUtilitiesConfig.afk.getNotificationTimer() ) {
            		afkPlayers++;
            	}
            }
            
        }
        
        
        int retVal = 0;
        if(!Morpheus.includeMiners)
        	retVal += miningPlayers;
        if(Morpheus.ignoreAfkPlayers)
        	retVal += afkPlayers;
        if(Morpheus.ignorePlayersInSpawnArea)
        	retVal += spawnPlayers;
        
        return retVal;
    }

	
    public int getSleepingPlayers()
    {
        int asleepCount = 0;
        for (Entry<String, Boolean> entry : this.playerStatus.entrySet())
        {
            if (entry.getValue())
            {
                asleepCount++;
            }
        }
        return asleepCount;
    }

    public String toString()
    {
    	boolean includeIgnored = false;
    	if(Morpheus.ignoreAfkPlayers || !Morpheus.includeMiners || Morpheus.ignorePlayersInSpawnArea)
    		includeIgnored = true;
 
    	return !includeIgnored ? this.getSleepingPlayers() + "/" + this.playerStatus.size() + " (" + this.getPercentSleeping() + "%)" : this.getSleepingPlayers() + "/" + this.playerStatus.size() + " - " + this.getIgnoredPlayers() + " AFK/Spawn (" + this.getPercentSleeping() + "%)";
    }

    public void setPlayerAsleep(String username)
    {
        this.playerStatus.put(username, true);
    }

    public void setPlayerAwake(String username)
    {
        this.playerStatus.put(username, false);
    }

    public boolean isPlayerSleeping(String username)
    {
        if (this.playerStatus.containsKey(username))
        {
            return this.playerStatus.get(username);
        }
        else
        {
            this.playerStatus.put(username, false);
        }
        return false;
    }

    public void removePlayer(String username)
    {
        this.playerStatus.remove(username);
    }

    public void wakeAllPlayers()
    {
        for (Entry<String, Boolean> entry : this.playerStatus.entrySet())
        {
            entry.setValue(false);
        }
    }
}
