package mods.EdibleTest.common;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockEdibleDirt extends BlockContainer {
	
	protected Random rand = new Random();
	
	public BlockEdibleDirt(int blockid)
	{
		super(blockid, Material.ground);
		this.setHardness(2.0F);
		this.setResistance(2.0F);
	}
	
	//右クリックされた時の処理
	//これとカマド処理に関してはほぼ、ModdingWiki様のチュートリアルの丸パクリ
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9){
		if (par1World.isRemote)
		{
			return true;
		}
		else
		{
			par5EntityPlayer.openGui(EdibleBlockTestMod.instance, EdibleBlockTestMod.instance.guiEdible, par1World, par2, par3, par4);
			return true;
		}
	}
	
	//周辺に中に入っていたアイテムをまき散らす
	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
	{
		TileEntityEdible tileentity = (TileEntityEdible) par1World.getBlockTileEntity(par2, par3, par4);
	 
		if (tileentity != null)
		{
			for (int j1 = 0; j1 < tileentity.inventory.getSizeInventory(); ++j1)
			{
				ItemStack itemstack = tileentity.inventory.getStackInSlot(j1);
	 
				if (itemstack != null)
				{
					float f = this.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = this.rand.nextFloat() * 0.8F + 0.1F;
	 
					while (itemstack.stackSize > 0)
					{
						int k1 = this.rand.nextInt(21) + 10;
	 
						if (k1 > itemstack.stackSize)
						{
							k1 = itemstack.stackSize;
						}
	 
						itemstack.stackSize -= k1;
						EntityItem entityitem = new EntityItem(par1World, (double)((float)par2 + f), (double)((float)par3 + f1), (double)((float)par4 + f2), new ItemStack(itemstack.itemID, k1, itemstack.getItemDamage()));
	 
						if (itemstack.hasTagCompound())
						{
							entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
						}
	 
						float f3 = 0.05F;
						entityitem.motionX = (double)((float)this.rand.nextGaussian() * f3);
						entityitem.motionY = (double)((float)this.rand.nextGaussian() * f3 + 0.2F);
						entityitem.motionZ = (double)((float)this.rand.nextGaussian() * f3);
						par1World.spawnEntityInWorld(entityitem);
					}
				}
			}
	 
			par1World.func_96440_m(par2, par3, par4, par5);
		}
	 
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}
	 
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityEdible();
	}
		
	@Override
	public int idDropped(int metadata, Random rand, int fortune)
	{
		return this.blockID;
	}
	
	@SideOnly(Side.CLIENT)
    public Icon getIcon(int par1, int par2)
    {
		return Block.dirt.getBlockTextureFromSide(0);
    }

}
