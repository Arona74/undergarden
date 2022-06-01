package quek.undergarden.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import quek.undergarden.Undergarden;
import quek.undergarden.entity.UGBoat;

import java.util.Map;
import java.util.stream.Stream;

public class UGBoatRenderer extends EntityRenderer<UGBoat> {

    private final Map<UGBoat.Type, Pair<ResourceLocation, BoatModel>> boatResources;

    public UGBoatRenderer(EntityRendererProvider.Context renderContext) {
        super(renderContext);
        this.shadowRadius = 0.8F;
        this.boatResources = Stream.of(UGBoat.Type.values()).collect(ImmutableMap.toImmutableMap((boatType) -> boatType,
                (boatType) -> Pair.of(new ResourceLocation(Undergarden.MODID, "textures/entity/boat/" + boatType.getName() + ".png"), new BoatModel(renderContext.bakeLayer(boatLayer(boatType))))));
    }

    public static ModelLayerLocation boatLayer(UGBoat.Type boatType) {
        return new ModelLayerLocation(new ResourceLocation(Undergarden.MODID, "boat/" + boatType.getName()), "main");
    }

    @Override
    public void render(UGBoat pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.0D, 0.375D, 0.0D);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pEntityYaw));
        float f = (float)pEntity.getHurtTime() - pPartialTicks;
        float f1 = pEntity.getDamage() - pPartialTicks;
        if (f1 < 0.0F) {
            f1 = 0.0F;
        }

        if (f > 0.0F) {
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * (float)pEntity.getHurtDir()));
        }

        float bubbleAngle = pEntity.getBubbleAngle(pPartialTicks);
        if (!Mth.equal(bubbleAngle, 0.0F)) {
            pMatrixStack.mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 1.0F), pEntity.getBubbleAngle(pPartialTicks), true));
        }

        Pair<ResourceLocation, BoatModel> pair = this.boatResources.get(pEntity.getUGBoatType());
        ResourceLocation resourcelocation = (ResourceLocation)pair.getFirst();
        BoatModel boatmodel = (BoatModel)pair.getSecond();
        pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        boatmodel.setupAnim(pEntity, pPartialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = pBuffer.getBuffer(boatmodel.renderType(resourcelocation));
        boatmodel.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        if (!pEntity.isUnderWater()) {
            VertexConsumer vertexconsumer1 = pBuffer.getBuffer(RenderType.waterMask());
            boatmodel.waterPatch().render(pMatrixStack, vertexconsumer1, pPackedLight, OverlayTexture.NO_OVERLAY);
        }

        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(UGBoat boat) {
        return this.boatResources.get(boat.getUGBoatType()).getFirst();
    }
}