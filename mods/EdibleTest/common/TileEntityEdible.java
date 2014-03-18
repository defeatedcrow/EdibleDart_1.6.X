package mods.EdibleTest.common;

import java.io.DataOutputStream;
import java.io.IOException;
 
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
 
import com.google.common.io.ByteArrayDataInput;
 
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityEdible extends TileEntity
{
 
	//燃焼時間
	public int burnTime;
 
	public int currentItemBurnTime;
 
	//調理時間
	public int cookTime;
 
	public InventoryEdible inventory;
 
	public TileEntityEdible() {
		this.inventory = new InventoryEdible(this);
	}
 
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
 
		//アイテムの読み込み
		NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items");
		this.inventory.sampleItemStacks = new ItemStack[this.inventory.getSizeInventory()];
 
		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
			byte b0 = nbttagcompound1.getByte("Slot");
 
			if (b0 >= 0 && b0 < this.inventory.sampleItemStacks.length)
			{
				this.inventory.sampleItemStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
 
		//燃焼時間や調理時間などの読み込み
		this.burnTime = par1NBTTagCompound.getShort("BurnTime");
		this.cookTime = par1NBTTagCompound.getShort("CookTime");
		this.currentItemBurnTime = getItemBurnTime(this.inventory.sampleItemStacks[1]);
 
	}
 
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
 
		//燃焼時間や調理時間などの書き込み
		par1NBTTagCompound.setShort("BurnTime", (short)this.burnTime);
		par1NBTTagCompound.setShort("CookTime", (short)this.cookTime);
 
		//アイテムの書き込み
		NBTTagList nbttaglist = new NBTTagList();
 
		for (int i = 0; i < this.inventory.sampleItemStacks.length; ++i)
		{
			if (this.inventory.sampleItemStacks[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte)i);
				this.inventory.sampleItemStacks[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
 
		par1NBTTagCompound.setTag("Items", nbttaglist);
 
	}
 
	public void readToPacket(ByteArrayDataInput data) {
		//アイテムの読み込み
		for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
			int id = data.readInt();
			int stacksize = data.readByte();
			int metadata = data.readInt();
 
			if (id != 0 && stacksize != 0) {
				this.inventory.setInventorySlotContents(i, new ItemStack(id, stacksize, metadata));
			} else {
				this.inventory.setInventorySlotContents(i, null);
			}
		}
	}
 
	public void writeToPacket(DataOutputStream dos) {
		try {
			//アイテムの書き込み
			for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
				int id = this.inventory.sampleItemStacks[i] != null ? this.inventory.sampleItemStacks[i].itemID : 0;
				int stacksize = this.inventory.sampleItemStacks[i] != null ? this.inventory.sampleItemStacks[i].stackSize : 0;
				int metadata = this.inventory.sampleItemStacks[i] != null ? this.inventory.sampleItemStacks[i].getItemDamage() : 0;
 
				dos.writeInt(id);
				dos.writeByte(stacksize);
				dos.writeInt(metadata);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	@Override
	public Packet getDescriptionPacket()
	{
		//パケットの取得
		return PacketHandler.getPacket(this);
	}
 
	//かまどの処理
	@SideOnly(Side.CLIENT)
	public int getCookProgressScaled(int par1)
	{
		return this.cookTime * par1 / 200;
	}
 
	//かまどの処理
	@SideOnly(Side.CLIENT)
	public int getBurnTimeRemainingScaled(int par1)
	{
		if (this.currentItemBurnTime == 0)
		{
			this.currentItemBurnTime = 200;
		}
 
		return this.burnTime * par1 / this.currentItemBurnTime;
	}
 
	//かまどの処理
	public boolean isBurning()
	{
		return this.burnTime > 0;
	}
 
	//更新時に呼び出される
	//かまどの処理
	public void updateEntity()
	{
		boolean flag = this.burnTime > 0;
		boolean flag1 = false;
 
		if (this.burnTime > 0)
		{
			--this.burnTime;
		}
 
		if (!this.worldObj.isRemote)
		{
			if (this.burnTime == 0 && this.canSmelt())
			{
				this.currentItemBurnTime = this.burnTime = getItemBurnTime(this.inventory.sampleItemStacks[1]);
 
				if (this.burnTime > 0)
				{
					flag1 = true;
 
					if (this.inventory.sampleItemStacks[1] != null)
					{
						--this.inventory.sampleItemStacks[1].stackSize;
 
						if (this.inventory.sampleItemStacks[1].stackSize == 0)
						{
							this.inventory.sampleItemStacks[1] = this.inventory.sampleItemStacks[1].getItem().getContainerItemStack(this.inventory.sampleItemStacks[1]);
						}
					}
				}
			}
 
			if (this.isBurning() && this.canSmelt())
			{
				++this.cookTime;
 
				if (this.cookTime == 200)
				{
					this.cookTime = 0;
					this.smeltItem();
					flag1 = true;
				}
			}
			else
			{
				this.cookTime = 0;
			}
 
			if (flag != this.burnTime > 0)
			{
				flag1 = true;
			}
		}
 
		if (flag1)
		{
			this.onInventoryChanged();
		}
	}
 
	//かまどの処理
	private boolean canSmelt()
	{
		if (this.inventory.sampleItemStacks[0] == null)
		{
			return false;
		}
		else
		{
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.inventory.sampleItemStacks[0]);
			if (itemstack == null) return false;
			if (this.inventory.sampleItemStacks[2] == null) return true;
			if (!this.inventory.sampleItemStacks[2].isItemEqual(itemstack)) return false;
			int result = this.inventory.sampleItemStacks[2].stackSize + itemstack.stackSize;
			return (result <= this.inventory.getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
		}
	}
 
	//かまどの処理
	public void smeltItem()
	{
		if (this.canSmelt())
		{
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.inventory.sampleItemStacks[0]);
 
			if (this.inventory.sampleItemStacks[2] == null)
			{
				this.inventory.sampleItemStacks[2] = itemstack.copy();
			}
			else if (this.inventory.sampleItemStacks[2].isItemEqual(itemstack))
			{
				this.inventory.sampleItemStacks[2].stackSize += itemstack.stackSize;
			}
 
			--this.inventory.sampleItemStacks[0].stackSize;
 
			if (this.inventory.sampleItemStacks[0].stackSize <= 0)
			{
				this.inventory.sampleItemStacks[0] = null;
			}
		}
	}
 
	//かまどの処理
	public static int getItemBurnTime(ItemStack par0ItemStack)
	{
		if (par0ItemStack == null)
		{
			return 0;
		}
		else
		{
			int i = par0ItemStack.getItem().itemID;
			Item item = par0ItemStack.getItem();
 
			if (par0ItemStack.getItem() instanceof ItemBlock && Block.blocksList[i] != null)
			{
				Block block = Block.blocksList[i];
 
				if (block == Block.woodSingleSlab)
				{
					return 150;
				}
 
				if (block.blockMaterial == Material.wood)
				{
					return 300;
				}
 
				if (block == Block.coalBlock)
				{
					return 16000;
				}
			}
 
			if (item instanceof ItemTool && ((ItemTool) item).getToolMaterialName().equals("WOOD")) return 200;
			if (item instanceof ItemSword && ((ItemSword) item).getToolMaterialName().equals("WOOD")) return 200;
			if (item instanceof ItemHoe && ((ItemHoe) item).getMaterialName().equals("WOOD")) return 200;
			if (i == Item.stick.itemID) return 100;
			if (i == Item.coal.itemID) return 1600;
			if (i == Item.bucketLava.itemID) return 20000;
			if (i == Block.sapling.blockID) return 100;
			if (i == Item.blazeRod.itemID) return 2400;
			return GameRegistry.getFuelValue(par0ItemStack);
		}
	}
 
	//かまどの処理
	public static boolean isItemFuel(ItemStack par0ItemStack)
	{
		return getItemBurnTime(par0ItemStack) > 0;
	}
 
}
