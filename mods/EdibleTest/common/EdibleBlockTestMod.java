package mods.EdibleTest.common;

import java.util.logging.Level;

import mods.EdibleTest.common.*;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(
		modid = "EdibleBlockTestMod",
		name = "EdibleBlocks",
		version = "1.6.2_1.0a"
		)
@NetworkMod(
		clientSideRequired = true,
		serverSideRequired = false,
		channels = "updateTile", packetHandler = PacketHandler.class
		)

public class EdibleBlockTestMod{
	
	@SidedProxy(clientSide = "mods.EdibleTest.client.ClientProxy", serverSide = "mods.EdibleTest.common.CommonProxy")
	public static CommonProxy proxy;
	
	@Instance("EdibleBlockTestMod")
    public static EdibleBlockTestMod instance;
	
	public static Block  edibleDirt;
	public static Item  edibleThing;
	
	public int blockIdEdibleDirt = 700;
	
	public static int guiEdible = 1;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
		try
		{
			cfg.load();
			Property blockEdible = cfg.getBlock("EdibleDirt", blockIdEdibleDirt);
			
			blockIdEdibleDirt = blockEdible.getInt();

		}
		catch (Exception e)
		{
			FMLLog.log(Level.SEVERE, e, "Error Message");

		}
		finally
		{
			cfg.save();
		}
		
		edibleDirt = (new BlockEdibleDirt(blockIdEdibleDirt)).
				setUnlocalizedName("defeatedcrow.edibleDirt").
				setCreativeTab(CreativeTabs.tabFood);
		
		GameRegistry.registerBlock(edibleDirt, ItemEdibleDirt.class, "EdibleDirt");
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		
		//Registering new recipe
		GameRegistry.addRecipe(
	    		  new ItemStack(this.edibleDirt, 1),
	    		  new Object[]{"Z","Z","Z",
	    			  Character.valueOf('Z'), Block.dirt
	    			  });
	      
		//TileEntityの登録
				GameRegistry.registerTileEntity(TileEntityEdible.class, "TileEntityEdible");
		 
				//GUIの登録
				NetworkRegistry.instance().registerGuiHandler(this, proxy);
		
	    //Registering language
		LanguageRegistry.addName(this.edibleDirt, "Edible Dirt");
		LanguageRegistry.instance().addNameForObject(this.edibleDirt, "ja_JP", "おいしい土");
	}
	
}
