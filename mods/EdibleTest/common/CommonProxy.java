package mods.EdibleTest.common;

import cpw.mods.fml.common.network.IGuiHandler;
import mods.EdibleTest.client.GuiEdible;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CommonProxy implements IGuiHandler{
	
	public void registerRenderers(){}

	public World getClientWorld() {
		
		return null;
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z))
			return null;
 
		TileEntity tileentity = world.getBlockTileEntity(x, y, z);
		if (tileentity != null && tileentity instanceof TileEntityEdible) {
			return new ContainerEdible(player, (TileEntityEdible) tileentity);
		}
		return null;
	}
 
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z))
			return null;
 
		TileEntity tileentity = world.getBlockTileEntity(x, y, z);
		if (tileentity != null && tileentity instanceof TileEntityEdible) {
			return new GuiEdible(player, (TileEntityEdible) tileentity);
		}
		return null;
	}

}
