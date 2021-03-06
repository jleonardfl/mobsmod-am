package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityCachalotEcho extends Entity {
    private static final DataParameter<Boolean> RETURNING = EntityDataManager.createKey(EntityCachalotEcho.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FASTER_ANIM = EntityDataManager.createKey(EntityCachalotEcho.class, DataSerializers.BOOLEAN);
    private UUID field_234609_b_;
    private int field_234610_c_;
    private boolean field_234611_d_;
    private boolean playerLaunched = false;

    public EntityCachalotEcho(EntityType p_i50162_1_, World p_i50162_2_) {
        super(p_i50162_1_, p_i50162_2_);
    }

    public EntityCachalotEcho(World worldIn, EntityCachalotWhale p_i47273_2_) {
        this(AMEntityRegistry.CACHALOT_ECHO, worldIn);
        this.setShooter(p_i47273_2_);
    }

    public EntityCachalotEcho(World worldIn, LivingEntity p_i47273_2_, boolean right) {
        this(AMEntityRegistry.CACHALOT_ECHO, worldIn);
        this.setShooter(p_i47273_2_);
        float rot = p_i47273_2_.rotationYawHead + (right ? 90 : -90);
        playerLaunched = true;
        this.setFasterAnimation(true);
        this.setPosition(p_i47273_2_.getPosX() - (double) (p_i47273_2_.getWidth()) * 0.5D * (double) MathHelper.sin(rot * ((float) Math.PI / 180F)), p_i47273_2_.getPosY() + 1D, p_i47273_2_.getPosZ() + (double) (p_i47273_2_.getWidth()) * 0.5D * (double) MathHelper.cos(rot * ((float) Math.PI / 180F)));
    }

    @OnlyIn(Dist.CLIENT)
    public EntityCachalotEcho(World worldIn, double x, double y, double z, double p_i47274_8_, double p_i47274_10_, double p_i47274_12_) {
        this(AMEntityRegistry.CACHALOT_ECHO, worldIn);
        this.setPosition(x, y, z);
        this.setMotion(p_i47274_8_, p_i47274_10_, p_i47274_12_);
    }

    public EntityCachalotEcho(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        this(AMEntityRegistry.CACHALOT_ECHO, world);
    }

    protected static float func_234614_e_(float p_234614_0_, float p_234614_1_) {
        while (p_234614_1_ - p_234614_0_ < -180.0F) {
            p_234614_0_ -= 360.0F;
        }

        while (p_234614_1_ - p_234614_0_ >= 180.0F) {
            p_234614_0_ += 360.0F;
        }

        return MathHelper.lerp(0.2F, p_234614_0_, p_234614_1_);
    }

    public boolean isReturning() {
        return this.dataManager.get(RETURNING).booleanValue();
    }

    public void setReturning(boolean returning) {
        this.dataManager.set(RETURNING, returning);
    }

    public boolean isFasterAnimation() {
        return this.dataManager.get(FASTER_ANIM).booleanValue();
    }

    public void setFasterAnimation(boolean anim) {
        this.dataManager.set(FASTER_ANIM, anim);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void tick() {
        double yMot = MathHelper.sqrt(this.getMotion().x * this.getMotion().x + this.getMotion().z * this.getMotion().z);
        this.rotationPitch = (float) (MathHelper.atan2(this.getMotion().y, yMot) * (double) (180F / (float) Math.PI));
        if (!this.field_234611_d_) {
            this.field_234611_d_ = this.func_234615_h_();
        }
        super.tick();
        Vector3d vector3d = this.getMotion();
        RayTraceResult raytraceresult = ProjectileHelper.func_234618_a_(this, this::func_230298_a_);
        if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
            this.onImpact(raytraceresult);
        }
        Entity shooter = this.func_234616_v_();
        if(this.isReturning() && shooter instanceof EntityCachalotWhale){
            EntityCachalotWhale whale = (EntityCachalotWhale)shooter;
            if(whale.headPart.getDistance(this) < whale.headPart.getWidth()){
                this.remove();
                whale.recieveEcho();
            }

        }
        if(!playerLaunched && !world.isRemote && !this.isInWaterOrBubbleColumn()){
            remove();
        }
        if (this.ticksExisted > 100) {
            remove();
        }

        double d0 = this.getPosX() + vector3d.x;
        double d1 = this.getPosY() + vector3d.y;
        double d2 = this.getPosZ() + vector3d.z;

        this.func_234617_x_();
        float f = 0.99F;
        float f1 = 0.06F;
        if(playerLaunched){
            this.noClip = true;
        }
        this.setMotion(vector3d.scale(0.99F));
        this.setNoGravity(true);
        this.setPosition(d0, d1, d2);
        this.rotationYaw = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI)) - 90;

    }

    protected void onEntityHit(EntityRayTraceResult result) {
        Entity entity = this.func_234616_v_();
        if (isReturning()) {
            EntityCachalotWhale whale = null;
            if (entity instanceof EntityCachalotWhale) {
                whale = (EntityCachalotWhale) entity;
                if (result.getEntity() instanceof EntityCachalotWhale || result.getEntity() instanceof EntityCachalotPart) {
                    whale.recieveEcho();
                    this.remove();
                }
            }
        } else if (result.getEntity() != entity && !result.getEntity().isEntityEqual(entity)) {
            this.setReturning(true);
            if (entity instanceof EntityCachalotWhale) {
                Vector3d vec = ((EntityCachalotWhale) entity).getReturnEchoVector();
                double d0 = vec.getX() - this.getPosX();
                double d1 = vec.getY() - this.getPosY();
                double d2 = vec.getZ() - this.getPosZ();
                this.setMotion(Vector3d.ZERO);
                EntityCachalotEcho echo = new EntityCachalotEcho(this.world, ((EntityCachalotWhale) entity));
                echo.copyLocationAndAnglesFrom(this);
                this.remove();
                echo.setReturning(true);
                echo.shoot(d0, d1, d2, 1, 0);
                if (!world.isRemote) {
                    world.addEntity(echo);
                }
            }
        }
    }

    protected void func_230299_a_(BlockRayTraceResult p_230299_1_) {
        BlockState blockstate = this.world.getBlockState(p_230299_1_.getPos());
        if (!this.world.isRemote && !playerLaunched) {
            this.remove();
        }
    }

    protected void registerData() {
        this.dataManager.register(RETURNING, false);
        this.dataManager.register(FASTER_ANIM, false);
    }

    public void setShooter(@Nullable Entity entityIn) {
        if (entityIn != null) {
            this.field_234609_b_ = entityIn.getUniqueID();
            this.field_234610_c_ = entityIn.getEntityId();
        }

    }

    @Nullable
    public Entity func_234616_v_() {
        if (this.field_234609_b_ != null && this.world instanceof ServerWorld) {
            return ((ServerWorld) this.world).getEntityByUuid(this.field_234609_b_);
        } else {
            return this.field_234610_c_ != 0 ? this.world.getEntityByID(this.field_234610_c_) : null;
        }
    }

    protected void writeAdditional(CompoundNBT compound) {
        if (this.field_234609_b_ != null) {
            compound.putUniqueId("Owner", this.field_234609_b_);
        }

        if (this.field_234611_d_) {
            compound.putBoolean("LeftOwner", true);
        }

    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readAdditional(CompoundNBT compound) {
        if (compound.hasUniqueId("Owner")) {
            this.field_234609_b_ = compound.getUniqueId("Owner");
        }

        this.field_234611_d_ = compound.getBoolean("LeftOwner");
    }

    private boolean func_234615_h_() {
        Entity entity = this.func_234616_v_();
        if (entity != null) {
            for (Entity entity1 : this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (p_234613_0_) -> {
                return !p_234613_0_.isSpectator() && p_234613_0_.canBeCollidedWith();
            })) {
                if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vector3d vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy, this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy, this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy).scale(velocity);
        this.setMotion(vector3d);
        float f = MathHelper.sqrt(horizontalMag(vector3d));
        this.rotationYaw = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(vector3d.y, f) * (double) (180F / (float) Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    public void func_234612_a_(Entity p_234612_1_, float p_234612_2_, float p_234612_3_, float p_234612_4_, float p_234612_5_, float p_234612_6_) {
        float f = -MathHelper.sin(p_234612_3_ * ((float) Math.PI / 180F)) * MathHelper.cos(p_234612_2_ * ((float) Math.PI / 180F));
        float f1 = -MathHelper.sin((p_234612_2_ + p_234612_4_) * ((float) Math.PI / 180F));
        float f2 = MathHelper.cos(p_234612_3_ * ((float) Math.PI / 180F)) * MathHelper.cos(p_234612_2_ * ((float) Math.PI / 180F));
        this.shoot(f, f1, f2, p_234612_5_, p_234612_6_);
        Vector3d vector3d = p_234612_1_.getMotion();
        this.setMotion(this.getMotion().add(vector3d.x, p_234612_1_.isOnGround() ? 0.0D : vector3d.y, vector3d.z));
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    protected void onImpact(RayTraceResult result) {
        RayTraceResult.Type raytraceresult$type = result.getType();
        if(playerLaunched){
            return;
        }
        if (raytraceresult$type == RayTraceResult.Type.ENTITY) {
            this.onEntityHit((EntityRayTraceResult) result);
        } else if (raytraceresult$type == RayTraceResult.Type.BLOCK) {
            this.func_230299_a_((BlockRayTraceResult) result);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.setMotion(x, y, z);
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            this.rotationPitch = (float) (MathHelper.atan2(y, f) * (double) (180F / (float) Math.PI));
            this.rotationYaw = (float) (MathHelper.atan2(x, z) * (double) (180F / (float) Math.PI));
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, this.rotationPitch);
        }

    }

    protected boolean func_230298_a_(Entity p_230298_1_) {
        if(playerLaunched){
            return false;
        }
        if (this.isReturning()) {
            return p_230298_1_ instanceof EntityCachalotPart || p_230298_1_ instanceof EntityCachalotWhale;
        } else if (p_230298_1_ instanceof EntityCachalotPart) {
            return false;
        }
        if (!p_230298_1_.isSpectator() && p_230298_1_.isAlive() && p_230298_1_.canBeCollidedWith()) {
            Entity entity = this.func_234616_v_();
            return (entity == null || this.field_234611_d_ || !entity.isRidingSameEntity(p_230298_1_));
        } else {
            return false;
        }
    }

    protected void func_234617_x_() {
        Vector3d vector3d = this.getMotion();
        float f = MathHelper.sqrt(horizontalMag(vector3d));
        this.rotationPitch = func_234614_e_(this.prevRotationPitch, (float) (MathHelper.atan2(vector3d.y, f) * (double) (180F / (float) Math.PI)));
        this.rotationYaw = func_234614_e_(this.prevRotationYaw, (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI)));
    }
}