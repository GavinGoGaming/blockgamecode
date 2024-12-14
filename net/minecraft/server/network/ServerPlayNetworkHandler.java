/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.command.argument.SignedArgumentList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.message.AcknowledgmentValidator;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageChainTaskQueue;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.AcknowledgeChunksC2SPacket;
import net.minecraft.network.packet.c2s.play.AcknowledgeReconfigurationC2SPacket;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.DebugSampleSubscriptionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryEntityNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlayNetworkHandler
extends ServerCommonNetworkHandler
implements ServerPlayPacketListener,
PlayerAssociatedNetworkHandler,
TickablePacketListener {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_SEQUENCE = -1;
    private static final int MAX_PENDING_ACKNOWLEDGMENTS = 4096;
    private static final int field_49027 = 80;
    private static final Text CHAT_VALIDATION_FAILED_TEXT = Text.translatable("multiplayer.disconnect.chat_validation_failed");
    private static final Text INVALID_COMMAND_SIGNATURE_TEXT = Text.translatable("chat.disabled.invalid_command_signature").formatted(Formatting.RED);
    private static final int field_49778 = 1000;
    public ServerPlayerEntity player;
    public final ChunkDataSender chunkDataSender;
    private int ticks;
    private int sequence = -1;
    private int messageCooldown;
    private int creativeItemDropThreshold;
    private double lastTickX;
    private double lastTickY;
    private double lastTickZ;
    private double updatedX;
    private double updatedY;
    private double updatedZ;
    @Nullable
    private Entity topmostRiddenEntity;
    private double lastTickRiddenX;
    private double lastTickRiddenY;
    private double lastTickRiddenZ;
    private double updatedRiddenX;
    private double updatedRiddenY;
    private double updatedRiddenZ;
    @Nullable
    private Vec3d requestedTeleportPos;
    private int requestedTeleportId;
    private int prevTeleportCheckTicks;
    private boolean floating;
    private int floatingTicks;
    private boolean vehicleFloating;
    private int vehicleFloatingTicks;
    private int movePacketsCount;
    private int lastTickMovePacketsCount;
    @Nullable
    private PublicPlayerSession session;
    private MessageChain.Unpacker messageUnpacker;
    private final AcknowledgmentValidator acknowledgmentValidator = new AcknowledgmentValidator(20);
    private final MessageSignatureStorage signatureStorage = MessageSignatureStorage.create();
    private final MessageChainTaskQueue messageChainTaskQueue;
    private boolean requestedReconfiguration;

    public ServerPlayNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
        super(server, connection, clientData);
        this.chunkDataSender = new ChunkDataSender(connection.isLocal());
        this.player = player;
        player.networkHandler = this;
        player.getTextStream().onConnect();
        this.messageUnpacker = MessageChain.Unpacker.unsigned(player.getUuid(), server::shouldEnforceSecureProfile);
        this.messageChainTaskQueue = new MessageChainTaskQueue(server);
    }

    @Override
    public void tick() {
        if (this.sequence > -1) {
            this.sendPacket(new PlayerActionResponseS2CPacket(this.sequence));
            this.sequence = -1;
        }
        this.syncWithPlayerPosition();
        this.player.prevX = this.player.getX();
        this.player.prevY = this.player.getY();
        this.player.prevZ = this.player.getZ();
        this.player.playerTick();
        this.player.updatePositionAndAngles(this.lastTickX, this.lastTickY, this.lastTickZ, this.player.getYaw(), this.player.getPitch());
        ++this.ticks;
        this.lastTickMovePacketsCount = this.movePacketsCount;
        if (this.floating && !this.player.isSleeping() && !this.player.hasVehicle() && !this.player.isDead()) {
            if (++this.floatingTicks > this.getMaxAllowedFloatingTicks(this.player)) {
                LOGGER.warn("{} was kicked for floating too long!", (Object)this.player.getName().getString());
                this.disconnect(Text.translatable("multiplayer.disconnect.flying"));
                return;
            }
        } else {
            this.floating = false;
            this.floatingTicks = 0;
        }
        this.topmostRiddenEntity = this.player.getRootVehicle();
        if (this.topmostRiddenEntity == this.player || this.topmostRiddenEntity.getControllingPassenger() != this.player) {
            this.topmostRiddenEntity = null;
            this.vehicleFloating = false;
            this.vehicleFloatingTicks = 0;
        } else {
            this.lastTickRiddenX = this.topmostRiddenEntity.getX();
            this.lastTickRiddenY = this.topmostRiddenEntity.getY();
            this.lastTickRiddenZ = this.topmostRiddenEntity.getZ();
            this.updatedRiddenX = this.topmostRiddenEntity.getX();
            this.updatedRiddenY = this.topmostRiddenEntity.getY();
            this.updatedRiddenZ = this.topmostRiddenEntity.getZ();
            if (this.vehicleFloating && this.topmostRiddenEntity.getControllingPassenger() == this.player) {
                if (++this.vehicleFloatingTicks > this.getMaxAllowedFloatingTicks(this.topmostRiddenEntity)) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", (Object)this.player.getName().getString());
                    this.disconnect(Text.translatable("multiplayer.disconnect.flying"));
                    return;
                }
            } else {
                this.vehicleFloating = false;
                this.vehicleFloatingTicks = 0;
            }
        }
        this.baseTick();
        if (this.messageCooldown > 0) {
            --this.messageCooldown;
        }
        if (this.creativeItemDropThreshold > 0) {
            --this.creativeItemDropThreshold;
        }
        if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMeasuringTimeMs() - this.player.getLastActionTime() > (long)this.server.getPlayerIdleTimeout() * 1000L * 60L) {
            this.disconnect(Text.translatable("multiplayer.disconnect.idling"));
        }
    }

    private int getMaxAllowedFloatingTicks(Entity vehicle) {
        double d = vehicle.getFinalGravity();
        if (d < (double)1.0E-5f) {
            return Integer.MAX_VALUE;
        }
        double e = 0.08 / d;
        return MathHelper.ceil(80.0 * Math.max(e, 1.0));
    }

    public void syncWithPlayerPosition() {
        this.lastTickX = this.player.getX();
        this.lastTickY = this.player.getY();
        this.lastTickZ = this.player.getZ();
        this.updatedX = this.player.getX();
        this.updatedY = this.player.getY();
        this.updatedZ = this.player.getZ();
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen() && !this.requestedReconfiguration;
    }

    @Override
    public boolean accepts(Packet<?> packet) {
        if (super.accepts(packet)) {
            return true;
        }
        return this.requestedReconfiguration && this.connection.isOpen() && packet instanceof AcknowledgeReconfigurationC2SPacket;
    }

    @Override
    protected GameProfile getProfile() {
        return this.player.getGameProfile();
    }

    private <T, R> CompletableFuture<R> filterText(T text, BiFunction<TextStream, T, CompletableFuture<R>> filterer) {
        return filterer.apply(this.player.getTextStream(), (TextStream)text).thenApply(filtered -> {
            if (!this.isConnectionOpen()) {
                LOGGER.debug("Ignoring packet due to disconnection");
                throw new CancellationException("disconnected");
            }
            return filtered;
        });
    }

    private CompletableFuture<FilteredMessage> filterText(String text) {
        return this.filterText(text, TextStream::filterText);
    }

    private CompletableFuture<List<FilteredMessage>> filterTexts(List<String> texts) {
        return this.filterText(texts, TextStream::filterTexts);
    }

    @Override
    public void onPlayerInput(PlayerInputC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.updateInput(packet.getSideways(), packet.getForward(), packet.isJumping(), packet.isSneaking());
    }

    private static boolean isMovementInvalid(double x, double y, double z, float yaw, float pitch) {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || !Floats.isFinite(pitch) || !Floats.isFinite(yaw);
    }

    private static double clampHorizontal(double d) {
        return MathHelper.clamp(d, -3.0E7, 3.0E7);
    }

    private static double clampVertical(double d) {
        return MathHelper.clamp(d, -2.0E7, 2.0E7);
    }

    @Override
    public void onVehicleMove(VehicleMoveC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (ServerPlayNetworkHandler.isMovementInvalid(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch())) {
            this.disconnect(Text.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
            return;
        }
        if (this.handlePendingTeleport()) {
            return;
        }
        Entity lv = this.player.getRootVehicle();
        if (lv != this.player && lv.getControllingPassenger() == this.player && lv == this.topmostRiddenEntity) {
            LivingEntity lv3;
            ServerWorld lv2 = this.player.getServerWorld();
            double d = lv.getX();
            double e = lv.getY();
            double f = lv.getZ();
            double g = ServerPlayNetworkHandler.clampHorizontal(packet.getX());
            double h = ServerPlayNetworkHandler.clampVertical(packet.getY());
            double i = ServerPlayNetworkHandler.clampHorizontal(packet.getZ());
            float j = MathHelper.wrapDegrees(packet.getYaw());
            float k = MathHelper.wrapDegrees(packet.getPitch());
            double l = g - this.lastTickRiddenX;
            double m = h - this.lastTickRiddenY;
            double n = i - this.lastTickRiddenZ;
            double p = l * l + m * m + n * n;
            double o = lv.getVelocity().lengthSquared();
            if (p - o > 100.0 && !this.isHost()) {
                LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", lv.getName().getString(), this.player.getName().getString(), l, m, n);
                this.sendPacket(new VehicleMoveS2CPacket(lv));
                return;
            }
            boolean bl = lv2.isSpaceEmpty(lv, lv.getBoundingBox().contract(0.0625));
            l = g - this.updatedRiddenX;
            m = h - this.updatedRiddenY - 1.0E-6;
            n = i - this.updatedRiddenZ;
            boolean bl2 = lv.groundCollision;
            if (lv instanceof LivingEntity && (lv3 = (LivingEntity)lv).isClimbing()) {
                lv3.onLanding();
            }
            lv.move(MovementType.PLAYER, new Vec3d(l, m, n));
            double q = m;
            l = g - lv.getX();
            m = h - lv.getY();
            if (m > -0.5 || m < 0.5) {
                m = 0.0;
            }
            n = i - lv.getZ();
            p = l * l + m * m + n * n;
            boolean bl3 = false;
            if (p > 0.0625) {
                bl3 = true;
                LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", lv.getName().getString(), this.player.getName().getString(), Math.sqrt(p));
            }
            lv.updatePositionAndAngles(g, h, i, j, k);
            boolean bl4 = lv2.isSpaceEmpty(lv, lv.getBoundingBox().contract(0.0625));
            if (bl && (bl3 || !bl4)) {
                lv.updatePositionAndAngles(d, e, f, j, k);
                this.sendPacket(new VehicleMoveS2CPacket(lv));
                return;
            }
            this.player.getServerWorld().getChunkManager().updatePosition(this.player);
            Vec3d lv4 = new Vec3d(lv.getX() - d, lv.getY() - e, lv.getZ() - f);
            this.player.setOnGround(lv4);
            this.player.increaseTravelMotionStats(lv4.x, lv4.y, lv4.z);
            this.vehicleFloating = q >= -0.03125 && !bl2 && !this.server.isFlightEnabled() && !lv.hasNoGravity() && this.isEntityOnAir(lv);
            this.updatedRiddenX = lv.getX();
            this.updatedRiddenY = lv.getY();
            this.updatedRiddenZ = lv.getZ();
        }
    }

    private boolean isEntityOnAir(Entity entity) {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    @Override
    public void onTeleportConfirm(TeleportConfirmC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (packet.getTeleportId() == this.requestedTeleportId) {
            if (this.requestedTeleportPos == null) {
                this.disconnect(Text.translatable("multiplayer.disconnect.invalid_player_movement"));
                return;
            }
            this.player.updatePositionAndAngles(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.getYaw(), this.player.getPitch());
            this.updatedX = this.requestedTeleportPos.x;
            this.updatedY = this.requestedTeleportPos.y;
            this.updatedZ = this.requestedTeleportPos.z;
            if (this.player.isInTeleportationState()) {
                this.player.onTeleportationDone();
            }
            this.requestedTeleportPos = null;
        }
    }

    @Override
    public void onRecipeBookData(RecipeBookDataC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.server.getRecipeManager().get(packet.getRecipeId()).ifPresent(this.player.getRecipeBook()::onRecipeDisplayed);
    }

    @Override
    public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.getRecipeBook().setCategoryOptions(packet.getCategory(), packet.isGuiOpen(), packet.isFilteringCraftable());
    }

    @Override
    public void onAdvancementTab(AdvancementTabC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (packet.getAction() == AdvancementTabC2SPacket.Action.OPENED_TAB) {
            Identifier lv = Objects.requireNonNull(packet.getTabToOpen());
            AdvancementEntry lv2 = this.server.getAdvancementLoader().get(lv);
            if (lv2 != null) {
                this.player.getAdvancementTracker().setDisplayTab(lv2);
            }
        }
    }

    @Override
    public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        StringReader stringReader = new StringReader(packet.getPartialCommand());
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }
        ParseResults<ServerCommandSource> parseResults = this.server.getCommandManager().getDispatcher().parse(stringReader, this.player.getCommandSource());
        this.server.getCommandManager().getDispatcher().getCompletionSuggestions(parseResults).thenAccept(suggestions -> {
            Suggestions suggestions2 = suggestions.getList().size() <= 1000 ? suggestions : new Suggestions(suggestions.getRange(), suggestions.getList().subList(0, 1000));
            this.sendPacket(new CommandSuggestionsS2CPacket(packet.getCompletionId(), suggestions2));
        });
    }

    @Override
    public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.server.areCommandBlocksEnabled()) {
            this.player.sendMessage(Text.translatable("advMode.notEnabled"));
            return;
        }
        if (!this.player.isCreativeLevelTwoOp()) {
            this.player.sendMessage(Text.translatable("advMode.notAllowed"));
            return;
        }
        CommandBlockExecutor lv = null;
        CommandBlockBlockEntity lv2 = null;
        BlockPos lv3 = packet.getPos();
        BlockEntity lv4 = this.player.getWorld().getBlockEntity(lv3);
        if (lv4 instanceof CommandBlockBlockEntity) {
            lv2 = (CommandBlockBlockEntity)lv4;
            lv = lv2.getCommandExecutor();
        }
        String string = packet.getCommand();
        boolean bl = packet.shouldTrackOutput();
        if (lv != null) {
            CommandBlockBlockEntity.Type lv5 = lv2.getCommandBlockType();
            BlockState lv6 = this.player.getWorld().getBlockState(lv3);
            Direction lv7 = lv6.get(CommandBlock.FACING);
            BlockState lv8 = switch (packet.getType()) {
                case CommandBlockBlockEntity.Type.SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                case CommandBlockBlockEntity.Type.AUTO -> Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                default -> Blocks.COMMAND_BLOCK.getDefaultState();
            };
            BlockState lv9 = (BlockState)((BlockState)lv8.with(CommandBlock.FACING, lv7)).with(CommandBlock.CONDITIONAL, packet.isConditional());
            if (lv9 != lv6) {
                this.player.getWorld().setBlockState(lv3, lv9, Block.NOTIFY_LISTENERS);
                lv4.setCachedState(lv9);
                this.player.getWorld().getWorldChunk(lv3).setBlockEntity(lv4);
            }
            lv.setCommand(string);
            lv.setTrackOutput(bl);
            if (!bl) {
                lv.setLastOutput(null);
            }
            lv2.setAuto(packet.isAlwaysActive());
            if (lv5 != packet.getType()) {
                lv2.updateCommandBlock();
            }
            lv.markDirty();
            if (!StringHelper.isEmpty(string)) {
                this.player.sendMessage(Text.translatable("advMode.setCommand.success", string));
            }
        }
    }

    @Override
    public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.server.areCommandBlocksEnabled()) {
            this.player.sendMessage(Text.translatable("advMode.notEnabled"));
            return;
        }
        if (!this.player.isCreativeLevelTwoOp()) {
            this.player.sendMessage(Text.translatable("advMode.notAllowed"));
            return;
        }
        CommandBlockExecutor lv = packet.getMinecartCommandExecutor(this.player.getWorld());
        if (lv != null) {
            lv.setCommand(packet.getCommand());
            lv.setTrackOutput(packet.shouldTrackOutput());
            if (!packet.shouldTrackOutput()) {
                lv.setLastOutput(null);
            }
            lv.markDirty();
            this.player.sendMessage(Text.translatable("advMode.setCommand.success", packet.getCommand()));
        }
    }

    @Override
    public void onPickFromInventory(PickFromInventoryC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.getInventory().swapSlotWithHotbar(packet.getSlot());
        this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, this.player.getInventory().selectedSlot, this.player.getInventory().getStack(this.player.getInventory().selectedSlot)));
        this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, packet.getSlot(), this.player.getInventory().getStack(packet.getSlot())));
        this.player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(this.player.getInventory().selectedSlot));
    }

    @Override
    public void onRenameItem(RenameItemC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        ScreenHandler screenHandler = this.player.currentScreenHandler;
        if (screenHandler instanceof AnvilScreenHandler) {
            AnvilScreenHandler lv = (AnvilScreenHandler)screenHandler;
            if (!lv.canUse(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)lv);
                return;
            }
            lv.setNewItemName(packet.getName());
        }
    }

    @Override
    public void onUpdateBeacon(UpdateBeaconC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        ScreenHandler screenHandler = this.player.currentScreenHandler;
        if (screenHandler instanceof BeaconScreenHandler) {
            BeaconScreenHandler lv = (BeaconScreenHandler)screenHandler;
            if (!this.player.currentScreenHandler.canUse(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.currentScreenHandler);
                return;
            }
            lv.setEffects(packet.primary(), packet.secondary());
        }
    }

    @Override
    public void onUpdateStructureBlock(UpdateStructureBlockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            return;
        }
        BlockPos lv = packet.getPos();
        BlockState lv2 = this.player.getWorld().getBlockState(lv);
        BlockEntity lv3 = this.player.getWorld().getBlockEntity(lv);
        if (lv3 instanceof StructureBlockBlockEntity) {
            StructureBlockBlockEntity lv4 = (StructureBlockBlockEntity)lv3;
            lv4.setMode(packet.getMode());
            lv4.setTemplateName(packet.getTemplateName());
            lv4.setOffset(packet.getOffset());
            lv4.setSize(packet.getSize());
            lv4.setMirror(packet.getMirror());
            lv4.setRotation(packet.getRotation());
            lv4.setMetadata(packet.getMetadata());
            lv4.setIgnoreEntities(packet.shouldIgnoreEntities());
            lv4.setShowAir(packet.shouldShowAir());
            lv4.setShowBoundingBox(packet.shouldShowBoundingBox());
            lv4.setIntegrity(packet.getIntegrity());
            lv4.setSeed(packet.getSeed());
            if (lv4.hasStructureName()) {
                String string = lv4.getTemplateName();
                if (packet.getAction() == StructureBlockBlockEntity.Action.SAVE_AREA) {
                    if (lv4.saveStructure()) {
                        this.player.sendMessage(Text.translatable("structure_block.save_success", string), false);
                    } else {
                        this.player.sendMessage(Text.translatable("structure_block.save_failure", string), false);
                    }
                } else if (packet.getAction() == StructureBlockBlockEntity.Action.LOAD_AREA) {
                    if (!lv4.isStructureAvailable()) {
                        this.player.sendMessage(Text.translatable("structure_block.load_not_found", string), false);
                    } else if (lv4.loadAndTryPlaceStructure(this.player.getServerWorld())) {
                        this.player.sendMessage(Text.translatable("structure_block.load_success", string), false);
                    } else {
                        this.player.sendMessage(Text.translatable("structure_block.load_prepare", string), false);
                    }
                } else if (packet.getAction() == StructureBlockBlockEntity.Action.SCAN_AREA) {
                    if (lv4.detectStructureSize()) {
                        this.player.sendMessage(Text.translatable("structure_block.size_success", string), false);
                    } else {
                        this.player.sendMessage(Text.translatable("structure_block.size_failure"), false);
                    }
                }
            } else {
                this.player.sendMessage(Text.translatable("structure_block.invalid_structure_name", packet.getTemplateName()), false);
            }
            lv4.markDirty();
            this.player.getWorld().updateListeners(lv, lv2, lv2, Block.NOTIFY_ALL);
        }
    }

    @Override
    public void onUpdateJigsaw(UpdateJigsawC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            return;
        }
        BlockPos lv = packet.getPos();
        BlockState lv2 = this.player.getWorld().getBlockState(lv);
        BlockEntity lv3 = this.player.getWorld().getBlockEntity(lv);
        if (lv3 instanceof JigsawBlockEntity) {
            JigsawBlockEntity lv4 = (JigsawBlockEntity)lv3;
            lv4.setName(packet.getName());
            lv4.setTarget(packet.getTarget());
            lv4.setPool(RegistryKey.of(RegistryKeys.TEMPLATE_POOL, packet.getPool()));
            lv4.setFinalState(packet.getFinalState());
            lv4.setJoint(packet.getJointType());
            lv4.setPlacementPriority(packet.getPlacementPriority());
            lv4.setSelectionPriority(packet.getSelectionPriority());
            lv4.markDirty();
            this.player.getWorld().updateListeners(lv, lv2, lv2, Block.NOTIFY_ALL);
        }
    }

    @Override
    public void onJigsawGenerating(JigsawGeneratingC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            return;
        }
        BlockPos lv = packet.getPos();
        BlockEntity lv2 = this.player.getWorld().getBlockEntity(lv);
        if (lv2 instanceof JigsawBlockEntity) {
            JigsawBlockEntity lv3 = (JigsawBlockEntity)lv2;
            lv3.generate(this.player.getServerWorld(), packet.getMaxDepth(), packet.shouldKeepJigsaws());
        }
    }

    @Override
    public void onSelectMerchantTrade(SelectMerchantTradeC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        int i = packet.getTradeId();
        ScreenHandler screenHandler = this.player.currentScreenHandler;
        if (screenHandler instanceof MerchantScreenHandler) {
            MerchantScreenHandler lv = (MerchantScreenHandler)screenHandler;
            if (!lv.canUse(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)lv);
                return;
            }
            lv.setRecipeIndex(i);
            lv.switchTo(i);
        }
    }

    @Override
    public void onBookUpdate(BookUpdateC2SPacket packet) {
        int i = packet.slot();
        if (!PlayerInventory.isValidHotbarIndex(i) && i != 40) {
            return;
        }
        ArrayList<String> list = Lists.newArrayList();
        Optional<String> optional = packet.title();
        optional.ifPresent(list::add);
        packet.pages().stream().limit(100L).forEach(list::add);
        Consumer<List> consumer = optional.isPresent() ? texts -> this.addBook((FilteredMessage)texts.get(0), texts.subList(1, texts.size()), i) : texts -> this.updateBookContent((List<FilteredMessage>)texts, i);
        this.filterTexts(list).thenAcceptAsync(consumer, (Executor)this.server);
    }

    private void updateBookContent(List<FilteredMessage> pages, int slotId) {
        ItemStack lv = this.player.getInventory().getStack(slotId);
        if (!lv.isOf(Items.WRITABLE_BOOK)) {
            return;
        }
        List<RawFilteredPair<String>> list2 = pages.stream().map(this::toRawFilteredPair).toList();
        lv.set(DataComponentTypes.WRITABLE_BOOK_CONTENT, new WritableBookContentComponent(list2));
    }

    private void addBook(FilteredMessage title, List<FilteredMessage> pages, int slotId) {
        ItemStack lv = this.player.getInventory().getStack(slotId);
        if (!lv.isOf(Items.WRITABLE_BOOK)) {
            return;
        }
        ItemStack lv2 = lv.withItem(Items.WRITTEN_BOOK);
        lv2.remove(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        List<RawFilteredPair<Text>> list2 = pages.stream().map(page -> this.toRawFilteredPair((FilteredMessage)page).map(Text::literal)).toList();
        lv2.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(this.toRawFilteredPair(title), this.player.getName().getString(), 0, list2, true));
        this.player.getInventory().setStack(slotId, lv2);
    }

    private RawFilteredPair<String> toRawFilteredPair(FilteredMessage message) {
        if (this.player.shouldFilterText()) {
            return RawFilteredPair.of(message.getString());
        }
        return RawFilteredPair.of(message);
    }

    @Override
    public void onQueryEntityNbt(QueryEntityNbtC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.player.hasPermissionLevel(2)) {
            return;
        }
        Entity lv = this.player.getWorld().getEntityById(packet.getEntityId());
        if (lv != null) {
            NbtCompound lv2 = lv.writeNbt(new NbtCompound());
            this.player.networkHandler.sendPacket(new NbtQueryResponseS2CPacket(packet.getTransactionId(), lv2));
        }
    }

    @Override
    public void onSlotChangedState(SlotChangedStateC2SPacket packet) {
        CrafterScreenHandler lv;
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (this.player.isSpectator() || packet.screenHandlerId() != this.player.currentScreenHandler.syncId) {
            return;
        }
        Object object = this.player.currentScreenHandler;
        if (object instanceof CrafterScreenHandler && (object = (lv = (CrafterScreenHandler)object).getInputInventory()) instanceof CrafterBlockEntity) {
            CrafterBlockEntity lv2 = (CrafterBlockEntity)object;
            lv2.setSlotEnabled(packet.slotId(), packet.newState());
        }
    }

    @Override
    public void onQueryBlockNbt(QueryBlockNbtC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.player.hasPermissionLevel(2)) {
            return;
        }
        BlockEntity lv = this.player.getWorld().getBlockEntity(packet.getPos());
        NbtCompound lv2 = lv != null ? lv.createNbt(this.player.getRegistryManager()) : null;
        this.player.networkHandler.sendPacket(new NbtQueryResponseS2CPacket(packet.getTransactionId(), lv2));
    }

    @Override
    public void onPlayerMove(PlayerMoveC2SPacket packet) {
        boolean bl2;
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (ServerPlayNetworkHandler.isMovementInvalid(packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0), packet.getYaw(0.0f), packet.getPitch(0.0f))) {
            this.disconnect(Text.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
        }
        ServerWorld lv = this.player.getServerWorld();
        if (this.player.notInAnyWorld) {
            return;
        }
        if (this.ticks == 0) {
            this.syncWithPlayerPosition();
        }
        if (this.handlePendingTeleport()) {
            return;
        }
        double d = ServerPlayNetworkHandler.clampHorizontal(packet.getX(this.player.getX()));
        double e = ServerPlayNetworkHandler.clampVertical(packet.getY(this.player.getY()));
        double f = ServerPlayNetworkHandler.clampHorizontal(packet.getZ(this.player.getZ()));
        float g = MathHelper.wrapDegrees(packet.getYaw(this.player.getYaw()));
        float h = MathHelper.wrapDegrees(packet.getPitch(this.player.getPitch()));
        if (this.player.hasVehicle()) {
            this.player.updatePositionAndAngles(this.player.getX(), this.player.getY(), this.player.getZ(), g, h);
            this.player.getServerWorld().getChunkManager().updatePosition(this.player);
            return;
        }
        double i = this.player.getX();
        double j = this.player.getY();
        double k = this.player.getZ();
        double l = d - this.lastTickX;
        double m = e - this.lastTickY;
        double n = f - this.lastTickZ;
        double o = this.player.getVelocity().lengthSquared();
        double p = l * l + m * m + n * n;
        if (this.player.isSleeping()) {
            if (p > 1.0) {
                this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), g, h);
            }
            return;
        }
        boolean bl = this.player.isFallFlying();
        if (lv.getTickManager().shouldTick()) {
            ++this.movePacketsCount;
            int q = this.movePacketsCount - this.lastTickMovePacketsCount;
            if (q > 5) {
                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", (Object)this.player.getName().getString(), (Object)q);
                q = 1;
            }
            if (!(this.player.isInTeleportationState() || this.player.getWorld().getGameRules().getBoolean(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK) && bl)) {
                float r;
                float f2 = r = bl ? 300.0f : 100.0f;
                if (p - o > (double)(r * (float)q) && !this.isHost()) {
                    LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), l, m, n);
                    this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYaw(), this.player.getPitch());
                    return;
                }
            }
        }
        Box lv2 = this.player.getBoundingBox();
        l = d - this.updatedX;
        m = e - this.updatedY;
        n = f - this.updatedZ;
        boolean bl3 = bl2 = m > 0.0;
        if (this.player.isOnGround() && !packet.isOnGround() && bl2) {
            this.player.jump();
        }
        boolean bl32 = this.player.groundCollision;
        this.player.move(MovementType.PLAYER, new Vec3d(l, m, n));
        double s = m;
        l = d - this.player.getX();
        m = e - this.player.getY();
        if (m > -0.5 || m < 0.5) {
            m = 0.0;
        }
        n = f - this.player.getZ();
        p = l * l + m * m + n * n;
        boolean bl4 = false;
        if (!this.player.isInTeleportationState() && p > 0.0625 && !this.player.isSleeping() && !this.player.interactionManager.isCreative() && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR) {
            bl4 = true;
            LOGGER.warn("{} moved wrongly!", (Object)this.player.getName().getString());
        }
        if (!this.player.noClip && !this.player.isSleeping() && (bl4 && lv.isSpaceEmpty(this.player, lv2) || this.isPlayerNotCollidingWithBlocks(lv, lv2, d, e, f))) {
            this.requestTeleport(i, j, k, g, h);
            this.player.handleFall(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k, packet.isOnGround());
            return;
        }
        this.player.updatePositionAndAngles(d, e, f, g, h);
        boolean bl5 = this.player.isUsingRiptide();
        this.floating = s >= -0.03125 && !bl32 && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR && !this.server.isFlightEnabled() && !this.player.getAbilities().allowFlying && !this.player.hasStatusEffect(StatusEffects.LEVITATION) && !bl && !bl5 && this.isEntityOnAir(this.player);
        this.player.getServerWorld().getChunkManager().updatePosition(this.player);
        Vec3d lv3 = new Vec3d(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k);
        this.player.setOnGround(packet.isOnGround(), lv3);
        this.player.handleFall(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k, packet.isOnGround());
        this.player.setOnGround(lv3);
        if (bl2) {
            this.player.onLanding();
        }
        if (packet.isOnGround() || this.player.hasLandedInFluid() || this.player.isClimbing() || this.player.isSpectator() || bl || bl5) {
            this.player.tryClearCurrentExplosion();
        }
        this.player.increaseTravelMotionStats(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k);
        this.updatedX = this.player.getX();
        this.updatedY = this.player.getY();
        this.updatedZ = this.player.getZ();
    }

    private boolean handlePendingTeleport() {
        if (this.requestedTeleportPos != null) {
            if (this.ticks - this.prevTeleportCheckTicks > 20) {
                this.prevTeleportCheckTicks = this.ticks;
                this.requestTeleport(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.getYaw(), this.player.getPitch());
            }
            return true;
        }
        this.prevTeleportCheckTicks = this.ticks;
        return false;
    }

    private boolean isPlayerNotCollidingWithBlocks(WorldView world, Box box, double newX, double newY, double newZ) {
        Box lv = this.player.getBoundingBox().offset(newX - this.player.getX(), newY - this.player.getY(), newZ - this.player.getZ());
        Iterable<VoxelShape> iterable = world.getCollisions(this.player, lv.contract(1.0E-5f));
        VoxelShape lv2 = VoxelShapes.cuboid(box.contract(1.0E-5f));
        for (VoxelShape lv3 : iterable) {
            if (VoxelShapes.matchesAnywhere(lv3, lv2, BooleanBiFunction.AND)) continue;
            return true;
        }
        return false;
    }

    public void requestTeleport(double x, double y, double z, float yaw, float pitch) {
        this.requestTeleport(x, y, z, yaw, pitch, Collections.emptySet());
    }

    public void requestTeleport(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags) {
        double i = flags.contains((Object)PositionFlag.X) ? this.player.getX() : 0.0;
        double j = flags.contains((Object)PositionFlag.Y) ? this.player.getY() : 0.0;
        double k = flags.contains((Object)PositionFlag.Z) ? this.player.getZ() : 0.0;
        float l = flags.contains((Object)PositionFlag.Y_ROT) ? this.player.getYaw() : 0.0f;
        float m = flags.contains((Object)PositionFlag.X_ROT) ? this.player.getPitch() : 0.0f;
        this.requestedTeleportPos = new Vec3d(x, y, z);
        if (++this.requestedTeleportId == Integer.MAX_VALUE) {
            this.requestedTeleportId = 0;
        }
        this.prevTeleportCheckTicks = this.ticks;
        this.player.updatePositionAndAngles(x, y, z, yaw, pitch);
        this.player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(x - i, y - j, z - k, yaw - l, pitch - m, flags, this.requestedTeleportId));
    }

    @Override
    public void onPlayerAction(PlayerActionC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        BlockPos lv = packet.getPos();
        this.player.updateLastActionTime();
        PlayerActionC2SPacket.Action lv2 = packet.getAction();
        switch (lv2) {
            case SWAP_ITEM_WITH_OFFHAND: {
                if (!this.player.isSpectator()) {
                    ItemStack lv3 = this.player.getStackInHand(Hand.OFF_HAND);
                    this.player.setStackInHand(Hand.OFF_HAND, this.player.getStackInHand(Hand.MAIN_HAND));
                    this.player.setStackInHand(Hand.MAIN_HAND, lv3);
                    this.player.clearActiveItem();
                }
                return;
            }
            case DROP_ITEM: {
                if (!this.player.isSpectator()) {
                    this.player.dropSelectedItem(false);
                }
                return;
            }
            case DROP_ALL_ITEMS: {
                if (!this.player.isSpectator()) {
                    this.player.dropSelectedItem(true);
                }
                return;
            }
            case RELEASE_USE_ITEM: {
                this.player.stopUsingItem();
                return;
            }
            case START_DESTROY_BLOCK: 
            case ABORT_DESTROY_BLOCK: 
            case STOP_DESTROY_BLOCK: {
                this.player.interactionManager.processBlockBreakingAction(lv, lv2, packet.getDirection(), this.player.getWorld().getTopY(), packet.getSequence());
                this.player.networkHandler.updateSequence(packet.getSequence());
                return;
            }
        }
        throw new IllegalArgumentException("Invalid player action");
    }

    private static boolean canPlace(ServerPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item lv = stack.getItem();
        return (lv instanceof BlockItem || lv instanceof BucketItem) && !player.getItemCooldownManager().isCoolingDown(lv);
    }

    @Override
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.networkHandler.updateSequence(packet.getSequence());
        ServerWorld lv = this.player.getServerWorld();
        Hand lv2 = packet.getHand();
        ItemStack lv3 = this.player.getStackInHand(lv2);
        if (!lv3.isItemEnabled(lv.getEnabledFeatures())) {
            return;
        }
        BlockHitResult lv4 = packet.getBlockHitResult();
        Vec3d lv5 = lv4.getPos();
        BlockPos lv6 = lv4.getBlockPos();
        if (!this.player.canInteractWithBlockAt(lv6, 1.0)) {
            return;
        }
        Vec3d lv7 = lv5.subtract(Vec3d.ofCenter(lv6));
        double d = 1.0000001;
        if (!(Math.abs(lv7.getX()) < 1.0000001 && Math.abs(lv7.getY()) < 1.0000001 && Math.abs(lv7.getZ()) < 1.0000001)) {
            LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", this.player.getGameProfile().getName(), lv5, lv6);
            return;
        }
        Direction lv8 = lv4.getSide();
        this.player.updateLastActionTime();
        int i = this.player.getWorld().getTopY();
        if (lv6.getY() < i) {
            if (this.requestedTeleportPos == null && lv.canPlayerModifyAt(this.player, lv6)) {
                ActionResult lv9 = this.player.interactionManager.interactBlock(this.player, lv, lv3, lv2, lv4);
                if (lv9.isAccepted()) {
                    Criteria.ANY_BLOCK_USE.trigger(this.player, lv4.getBlockPos(), lv3.copy());
                }
                if (lv8 == Direction.UP && !lv9.isAccepted() && lv6.getY() >= i - 1 && ServerPlayNetworkHandler.canPlace(this.player, lv3)) {
                    MutableText lv10 = Text.translatable("build.tooHigh", i - 1).formatted(Formatting.RED);
                    this.player.sendMessageToClient(lv10, true);
                } else if (lv9.shouldSwingHand()) {
                    this.player.swingHand(lv2, true);
                }
            }
        } else {
            MutableText lv11 = Text.translatable("build.tooHigh", i - 1).formatted(Formatting.RED);
            this.player.sendMessageToClient(lv11, true);
        }
        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(lv, lv6));
        this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(lv, lv6.offset(lv8)));
    }

    @Override
    public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet) {
        ActionResult lv4;
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.updateSequence(packet.getSequence());
        ServerWorld lv = this.player.getServerWorld();
        Hand lv2 = packet.getHand();
        ItemStack lv3 = this.player.getStackInHand(lv2);
        this.player.updateLastActionTime();
        if (lv3.isEmpty() || !lv3.isItemEnabled(lv.getEnabledFeatures())) {
            return;
        }
        float f = MathHelper.wrapDegrees(packet.getYaw());
        float g = MathHelper.wrapDegrees(packet.getPitch());
        if (g != this.player.getPitch() || f != this.player.getYaw()) {
            this.player.setAngles(f, g);
        }
        if ((lv4 = this.player.interactionManager.interactItem(this.player, lv, lv3, lv2)).shouldSwingHand()) {
            this.player.swingHand(lv2, true);
        }
    }

    @Override
    public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (this.player.isSpectator()) {
            for (ServerWorld lv : this.server.getWorlds()) {
                Entity lv2 = packet.getTarget(lv);
                if (lv2 == null) continue;
                this.player.teleport(lv, lv2.getX(), lv2.getY(), lv2.getZ(), lv2.getYaw(), lv2.getPitch());
                return;
            }
        }
    }

    @Override
    public void onBoatPaddleState(BoatPaddleStateC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        Entity lv = this.player.getControllingVehicle();
        if (lv instanceof BoatEntity) {
            BoatEntity lv2 = (BoatEntity)lv;
            lv2.setPaddleMovings(packet.isLeftPaddling(), packet.isRightPaddling());
        }
    }

    @Override
    public void onDisconnected(DisconnectionInfo info) {
        LOGGER.info("{} lost connection: {}", (Object)this.player.getName().getString(), (Object)info.reason().getString());
        this.cleanUp();
        super.onDisconnected(info);
    }

    private void cleanUp() {
        this.messageChainTaskQueue.close();
        this.server.forcePlayerSampleUpdate();
        this.server.getPlayerManager().broadcast(Text.translatable("multiplayer.player.left", this.player.getDisplayName()).formatted(Formatting.YELLOW), false);
        this.player.onDisconnect();
        this.server.getPlayerManager().remove(this.player);
        this.player.getTextStream().onDisconnect();
    }

    public void updateSequence(int sequence) {
        if (sequence < 0) {
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        }
        this.sequence = Math.max(sequence, this.sequence);
    }

    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (packet.getSelectedSlot() < 0 || packet.getSelectedSlot() >= PlayerInventory.getHotbarSize()) {
            LOGGER.warn("{} tried to set an invalid carried item", (Object)this.player.getName().getString());
            return;
        }
        if (this.player.getInventory().selectedSlot != packet.getSelectedSlot() && this.player.getActiveHand() == Hand.MAIN_HAND) {
            this.player.clearActiveItem();
        }
        this.player.getInventory().selectedSlot = packet.getSelectedSlot();
        this.player.updateLastActionTime();
    }

    @Override
    public void onChatMessage(ChatMessageC2SPacket packet) {
        Optional<LastSeenMessageList> optional = this.validateAcknowledgment(packet.acknowledgment());
        if (optional.isEmpty()) {
            return;
        }
        this.validateMessage(packet.chatMessage(), () -> {
            SignedMessage lv;
            try {
                lv = this.getSignedMessage(packet, (LastSeenMessageList)optional.get());
            } catch (MessageChain.MessageChainException lv2) {
                this.handleMessageChainException(lv2);
                return;
            }
            CompletableFuture<FilteredMessage> completableFuture = this.filterText(lv.getSignedContent());
            Text lv3 = this.server.getMessageDecorator().decorate(this.player, lv.getContent());
            this.messageChainTaskQueue.append(completableFuture, filtered -> {
                SignedMessage lv = lv.withUnsignedContent(lv3).withFilterMask(filtered.mask());
                this.handleDecoratedMessage(lv);
            });
        });
    }

    @Override
    public void onCommandExecution(CommandExecutionC2SPacket packet) {
        this.validateMessage(packet.command(), () -> {
            this.executeCommand(packet.command());
            this.checkForSpam();
        });
    }

    private void executeCommand(String command) {
        ParseResults<ServerCommandSource> parseResults = this.parse(command);
        if (this.server.shouldEnforceSecureProfile() && SignedArgumentList.isNotEmpty(parseResults)) {
            LOGGER.error("Received unsigned command packet from {}, but the command requires signable arguments: {}", (Object)this.player.getGameProfile().getName(), (Object)command);
            this.player.sendMessage(INVALID_COMMAND_SIGNATURE_TEXT);
            return;
        }
        this.server.getCommandManager().execute(parseResults, command);
    }

    @Override
    public void onChatCommandSigned(ChatCommandSignedC2SPacket packet) {
        Optional<LastSeenMessageList> optional = this.validateAcknowledgment(packet.lastSeenMessages());
        if (optional.isEmpty()) {
            return;
        }
        this.validateMessage(packet.command(), () -> {
            this.handleCommandExecution(packet, (LastSeenMessageList)optional.get());
            this.checkForSpam();
        });
    }

    private void handleCommandExecution(ChatCommandSignedC2SPacket packet, LastSeenMessageList lastSeenMessages) {
        Map<String, SignedMessage> map;
        ParseResults<ServerCommandSource> parseResults = this.parse(packet.command());
        try {
            map = this.collectArgumentMessages(packet, SignedArgumentList.of(parseResults), lastSeenMessages);
        } catch (MessageChain.MessageChainException lv) {
            this.handleMessageChainException(lv);
            return;
        }
        SignedCommandArguments.Impl lv2 = new SignedCommandArguments.Impl(map);
        parseResults = CommandManager.withCommandSource(parseResults, source -> source.withSignedArguments(lv2, this.messageChainTaskQueue));
        this.server.getCommandManager().execute(parseResults, packet.command());
    }

    private void handleMessageChainException(MessageChain.MessageChainException exception) {
        LOGGER.warn("Failed to update secure chat state for {}: '{}'", (Object)this.player.getGameProfile().getName(), (Object)exception.getMessageText().getString());
        this.player.sendMessage(exception.getMessageText().copy().formatted(Formatting.RED));
    }

    private <S> Map<String, SignedMessage> collectArgumentMessages(ChatCommandSignedC2SPacket packet, SignedArgumentList<S> arguments, LastSeenMessageList lastSeenMessages) throws MessageChain.MessageChainException {
        List<ArgumentSignatureDataMap.Entry> list = packet.argumentSignatures().entries();
        List<SignedArgumentList.ParsedArgument<S>> list2 = arguments.arguments();
        if (list.isEmpty()) {
            return this.toUnsignedSignatures(list2);
        }
        Object2ObjectOpenHashMap<String, SignedMessage> map = new Object2ObjectOpenHashMap<String, SignedMessage>();
        for (ArgumentSignatureDataMap.Entry entry : list) {
            SignedArgumentList.ParsedArgument<S> lv2 = arguments.get(entry.name());
            if (lv2 == null) {
                this.messageUnpacker.setChainBroken();
                throw ServerPlayNetworkHandler.createInvalidCommandSignatureException(packet.command(), list, list2);
            }
            MessageBody lv3 = new MessageBody(lv2.value(), packet.timestamp(), packet.salt(), lastSeenMessages);
            map.put(lv2.getNodeName(), this.messageUnpacker.unpack(entry.signature(), lv3));
        }
        for (SignedArgumentList.ParsedArgument parsedArgument : list2) {
            if (map.containsKey(parsedArgument.getNodeName())) continue;
            throw ServerPlayNetworkHandler.createInvalidCommandSignatureException(packet.command(), list, list2);
        }
        return map;
    }

    private <S> Map<String, SignedMessage> toUnsignedSignatures(List<SignedArgumentList.ParsedArgument<S>> arguments) throws MessageChain.MessageChainException {
        HashMap<String, SignedMessage> map = new HashMap<String, SignedMessage>();
        for (SignedArgumentList.ParsedArgument<S> lv : arguments) {
            MessageBody lv2 = MessageBody.ofUnsigned(lv.value());
            map.put(lv.getNodeName(), this.messageUnpacker.unpack(null, lv2));
        }
        return map;
    }

    private static <S> MessageChain.MessageChainException createInvalidCommandSignatureException(String command, List<ArgumentSignatureDataMap.Entry> actual, List<SignedArgumentList.ParsedArgument<S>> expected) {
        String string2 = actual.stream().map(ArgumentSignatureDataMap.Entry::name).collect(Collectors.joining(", "));
        String string3 = expected.stream().map(SignedArgumentList.ParsedArgument::getNodeName).collect(Collectors.joining(", "));
        LOGGER.error("Signed command mismatch between server and client ('{}'): got [{}] from client, but expected [{}]", command, string2, string3);
        return new MessageChain.MessageChainException(INVALID_COMMAND_SIGNATURE_TEXT);
    }

    private ParseResults<ServerCommandSource> parse(String command) {
        CommandDispatcher<ServerCommandSource> commandDispatcher = this.server.getCommandManager().getDispatcher();
        return commandDispatcher.parse(command, this.player.getCommandSource());
    }

    private void validateMessage(String message, Runnable callback) {
        if (ServerPlayNetworkHandler.hasIllegalCharacter(message)) {
            this.disconnect(Text.translatable("multiplayer.disconnect.illegal_characters"));
            return;
        }
        if (this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket(Text.translatable("chat.disabled.options").formatted(Formatting.RED), false));
            return;
        }
        this.player.updateLastActionTime();
        this.server.execute(callback);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Optional<LastSeenMessageList> validateAcknowledgment(LastSeenMessageList.Acknowledgment acknowledgment) {
        AcknowledgmentValidator acknowledgmentValidator = this.acknowledgmentValidator;
        synchronized (acknowledgmentValidator) {
            Optional<LastSeenMessageList> optional = this.acknowledgmentValidator.validate(acknowledgment);
            if (optional.isEmpty()) {
                LOGGER.warn("Failed to validate message acknowledgements from {}", (Object)this.player.getName().getString());
                this.disconnect(CHAT_VALIDATION_FAILED_TEXT);
            }
            return optional;
        }
    }

    private static boolean hasIllegalCharacter(String message) {
        for (int i = 0; i < message.length(); ++i) {
            if (StringHelper.isValidChar(message.charAt(i))) continue;
            return true;
        }
        return false;
    }

    private SignedMessage getSignedMessage(ChatMessageC2SPacket packet, LastSeenMessageList lastSeenMessages) throws MessageChain.MessageChainException {
        MessageBody lv = new MessageBody(packet.chatMessage(), packet.timestamp(), packet.salt(), lastSeenMessages);
        return this.messageUnpacker.unpack(packet.signature(), lv);
    }

    private void handleDecoratedMessage(SignedMessage message) {
        this.server.getPlayerManager().broadcast(message, this.player, MessageType.params(MessageType.CHAT, this.player));
        this.checkForSpam();
    }

    private void checkForSpam() {
        this.messageCooldown += 20;
        if (this.messageCooldown > 200 && !this.server.getPlayerManager().isOperator(this.player.getGameProfile()) && !this.server.isHost(this.player.getGameProfile())) {
            this.disconnect(Text.translatable("disconnect.spam"));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onMessageAcknowledgment(MessageAcknowledgmentC2SPacket packet) {
        AcknowledgmentValidator acknowledgmentValidator = this.acknowledgmentValidator;
        synchronized (acknowledgmentValidator) {
            if (!this.acknowledgmentValidator.removeUntil(packet.offset())) {
                LOGGER.warn("Failed to validate message acknowledgements from {}", (Object)this.player.getName().getString());
                this.disconnect(CHAT_VALIDATION_FAILED_TEXT);
            }
        }
    }

    @Override
    public void onHandSwing(HandSwingC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.updateLastActionTime();
        this.player.swingHand(packet.getHand());
    }

    @Override
    public void onClientCommand(ClientCommandC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.updateLastActionTime();
        switch (packet.getMode()) {
            case PRESS_SHIFT_KEY: {
                this.player.setSneaking(true);
                break;
            }
            case RELEASE_SHIFT_KEY: {
                this.player.setSneaking(false);
                break;
            }
            case START_SPRINTING: {
                this.player.setSprinting(true);
                break;
            }
            case STOP_SPRINTING: {
                this.player.setSprinting(false);
                break;
            }
            case STOP_SLEEPING: {
                if (!this.player.isSleeping()) break;
                this.player.wakeUp(false, true);
                this.requestedTeleportPos = this.player.getPos();
                break;
            }
            case START_RIDING_JUMP: {
                Entity entity = this.player.getControllingVehicle();
                if (!(entity instanceof JumpingMount)) break;
                JumpingMount lv = (JumpingMount)((Object)entity);
                int i = packet.getMountJumpHeight();
                if (!lv.canJump() || i <= 0) break;
                lv.startJumping(i);
                break;
            }
            case STOP_RIDING_JUMP: {
                Entity entity = this.player.getControllingVehicle();
                if (!(entity instanceof JumpingMount)) break;
                JumpingMount lv = (JumpingMount)((Object)entity);
                lv.stopJumping();
                break;
            }
            case OPEN_INVENTORY: {
                Entity entity = this.player.getVehicle();
                if (!(entity instanceof RideableInventory)) break;
                RideableInventory lv2 = (RideableInventory)((Object)entity);
                lv2.openInventory(this.player);
                break;
            }
            case START_FALL_FLYING: {
                if (this.player.checkFallFlying()) break;
                this.player.stopFallFlying();
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid client command!");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addPendingAcknowledgment(SignedMessage message) {
        int i;
        MessageSignatureData lv = message.signature();
        if (lv == null) {
            return;
        }
        this.signatureStorage.add(message.signedBody(), message.signature());
        AcknowledgmentValidator acknowledgmentValidator = this.acknowledgmentValidator;
        synchronized (acknowledgmentValidator) {
            this.acknowledgmentValidator.addPending(lv);
            i = this.acknowledgmentValidator.getMessageCount();
        }
        if (i > 4096) {
            this.disconnect(Text.translatable("multiplayer.disconnect.too_many_pending_chats"));
        }
    }

    public void sendChatMessage(SignedMessage message, MessageType.Parameters params) {
        this.sendPacket(new ChatMessageS2CPacket(message.link().sender(), message.link().index(), message.signature(), message.signedBody().toSerialized(this.signatureStorage), message.unsignedContent(), message.filterMask(), params));
        this.addPendingAcknowledgment(message);
    }

    public void sendProfilelessChatMessage(Text message, MessageType.Parameters params) {
        this.sendPacket(new ProfilelessChatMessageS2CPacket(message, params));
    }

    public SocketAddress getConnectionAddress() {
        return this.connection.getAddress();
    }

    public void reconfigure() {
        this.requestedReconfiguration = true;
        this.cleanUp();
        this.sendPacket(EnterReconfigurationS2CPacket.INSTANCE);
        this.connection.transitionOutbound(ConfigurationStates.S2C);
    }

    @Override
    public void onQueryPing(QueryPingC2SPacket packet) {
        this.connection.send(new PingResultS2CPacket(packet.getStartTime()));
    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        final ServerWorld lv = this.player.getServerWorld();
        final Entity lv2 = packet.getEntity(lv);
        this.player.updateLastActionTime();
        this.player.setSneaking(packet.isPlayerSneaking());
        if (lv2 != null) {
            if (!lv.getWorldBorder().contains(lv2.getBlockPos())) {
                return;
            }
            Box lv3 = lv2.getBoundingBox();
            if (this.player.canInteractWithEntityIn(lv3, 1.0)) {
                packet.handle(new PlayerInteractEntityC2SPacket.Handler(){

                    private void processInteract(Hand hand, Interaction action) {
                        ItemStack lv4 = ServerPlayNetworkHandler.this.player.getStackInHand(hand);
                        if (!lv4.isItemEnabled(lv.getEnabledFeatures())) {
                            return;
                        }
                        ItemStack lv22 = lv4.copy();
                        ActionResult lv3 = action.run(ServerPlayNetworkHandler.this.player, lv2, hand);
                        if (lv3.isAccepted()) {
                            Criteria.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerPlayNetworkHandler.this.player, lv3.shouldIncrementStat() ? lv22 : ItemStack.EMPTY, lv2);
                            if (lv3.shouldSwingHand()) {
                                ServerPlayNetworkHandler.this.player.swingHand(hand, true);
                            }
                        }
                    }

                    @Override
                    public void interact(Hand hand) {
                        this.processInteract(hand, PlayerEntity::interact);
                    }

                    @Override
                    public void interactAt(Hand hand, Vec3d pos) {
                        this.processInteract(hand, (player, entity, handx) -> entity.interactAt(player, pos, handx));
                    }

                    @Override
                    public void attack() {
                        PersistentProjectileEntity lv3;
                        if (lv2 instanceof ItemEntity || lv2 instanceof ExperienceOrbEntity || lv2 == ServerPlayNetworkHandler.this.player || lv2 instanceof PersistentProjectileEntity && !(lv3 = (PersistentProjectileEntity)lv2).isAttackable()) {
                            ServerPlayNetworkHandler.this.disconnect(Text.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                            LOGGER.warn("Player {} tried to attack an invalid entity", (Object)ServerPlayNetworkHandler.this.player.getName().getString());
                            return;
                        }
                        ItemStack lv22 = ServerPlayNetworkHandler.this.player.getStackInHand(Hand.MAIN_HAND);
                        if (!lv22.isItemEnabled(lv.getEnabledFeatures())) {
                            return;
                        }
                        ServerPlayNetworkHandler.this.player.attack(lv2);
                    }
                });
            }
        }
    }

    @Override
    public void onClientStatus(ClientStatusC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.updateLastActionTime();
        ClientStatusC2SPacket.Mode lv = packet.getMode();
        switch (lv) {
            case PERFORM_RESPAWN: {
                if (this.player.notInAnyWorld) {
                    this.player.notInAnyWorld = false;
                    this.player = this.server.getPlayerManager().respawnPlayer(this.player, true, Entity.RemovalReason.CHANGED_DIMENSION);
                    Criteria.CHANGED_DIMENSION.trigger(this.player, World.END, World.OVERWORLD);
                    break;
                }
                if (this.player.getHealth() > 0.0f) {
                    return;
                }
                this.player = this.server.getPlayerManager().respawnPlayer(this.player, false, Entity.RemovalReason.KILLED);
                if (!this.server.isHardcore()) break;
                this.player.changeGameMode(GameMode.SPECTATOR);
                this.player.getWorld().getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(false, this.server);
                break;
            }
            case REQUEST_STATS: {
                this.player.getStatHandler().sendStats(this.player);
            }
        }
    }

    @Override
    public void onCloseHandledScreen(CloseHandledScreenC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.onHandledScreenClosed();
    }

    @Override
    public void onClickSlot(ClickSlotC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.updateLastActionTime();
        if (this.player.currentScreenHandler.syncId != packet.getSyncId()) {
            return;
        }
        if (this.player.isSpectator()) {
            this.player.currentScreenHandler.syncState();
            return;
        }
        if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.currentScreenHandler);
            return;
        }
        int i = packet.getSlot();
        if (!this.player.currentScreenHandler.isValid(i)) {
            LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", this.player.getName(), i, this.player.currentScreenHandler.slots.size());
            return;
        }
        boolean bl = packet.getRevision() != this.player.currentScreenHandler.getRevision();
        this.player.currentScreenHandler.disableSyncing();
        this.player.currentScreenHandler.onSlotClick(i, packet.getButton(), packet.getActionType(), this.player);
        for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(packet.getModifiedStacks())) {
            this.player.currentScreenHandler.setPreviousTrackedSlotMutable(entry.getIntKey(), (ItemStack)entry.getValue());
        }
        this.player.currentScreenHandler.setPreviousCursorStack(packet.getStack());
        this.player.currentScreenHandler.enableSyncing();
        if (bl) {
            this.player.currentScreenHandler.updateToClient();
        } else {
            this.player.currentScreenHandler.sendContentUpdates();
        }
    }

    @Override
    public void onCraftRequest(CraftRequestC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.updateLastActionTime();
        if (this.player.isSpectator() || this.player.currentScreenHandler.syncId != packet.getSyncId() || !(this.player.currentScreenHandler instanceof AbstractRecipeScreenHandler)) {
            return;
        }
        if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.currentScreenHandler);
            return;
        }
        this.server.getRecipeManager().get(packet.getRecipeId()).ifPresent(recipe -> ((AbstractRecipeScreenHandler)this.player.currentScreenHandler).fillInputSlots(packet.shouldCraftAll(), (RecipeEntry<?>)recipe, this.player));
    }

    @Override
    public void onButtonClick(ButtonClickC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.updateLastActionTime();
        if (this.player.currentScreenHandler.syncId != packet.syncId() || this.player.isSpectator()) {
            return;
        }
        if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.currentScreenHandler);
            return;
        }
        boolean bl = this.player.currentScreenHandler.onButtonClick(this.player, packet.buttonId());
        if (bl) {
            this.player.currentScreenHandler.sendContentUpdates();
        }
    }

    @Override
    public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (this.player.interactionManager.isCreative()) {
            boolean bl3;
            boolean bl = packet.slot() < 0;
            ItemStack lv = packet.stack();
            if (!lv.isItemEnabled(this.player.getWorld().getEnabledFeatures())) {
                return;
            }
            NbtComponent lv2 = lv.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT);
            if (lv2.contains("x") && lv2.contains("y") && lv2.contains("z")) {
                BlockEntity lv4;
                BlockPos lv3 = BlockEntity.posFromNbt(lv2.getNbt());
                if (this.player.getWorld().canSetBlock(lv3) && (lv4 = this.player.getWorld().getBlockEntity(lv3)) != null) {
                    lv4.setStackNbt(lv, this.player.getWorld().getRegistryManager());
                }
            }
            boolean bl2 = packet.slot() >= 1 && packet.slot() <= 45;
            boolean bl4 = bl3 = lv.isEmpty() || lv.getCount() <= lv.getMaxCount();
            if (bl2 && bl3) {
                this.player.playerScreenHandler.getSlot(packet.slot()).setStack(lv);
                this.player.playerScreenHandler.sendContentUpdates();
            } else if (bl && bl3 && this.creativeItemDropThreshold < 200) {
                this.creativeItemDropThreshold += 20;
                this.player.dropItem(lv, true);
            }
        }
    }

    @Override
    public void onUpdateSign(UpdateSignC2SPacket packet) {
        List<String> list = Stream.of(packet.getText()).map(Formatting::strip).collect(Collectors.toList());
        this.filterTexts(list).thenAcceptAsync(texts -> this.onSignUpdate(packet, (List<FilteredMessage>)texts), (Executor)this.server);
    }

    private void onSignUpdate(UpdateSignC2SPacket packet, List<FilteredMessage> signText) {
        this.player.updateLastActionTime();
        ServerWorld lv = this.player.getServerWorld();
        BlockPos lv2 = packet.getPos();
        if (lv.isChunkLoaded(lv2)) {
            BlockEntity lv3 = lv.getBlockEntity(lv2);
            if (!(lv3 instanceof SignBlockEntity)) {
                return;
            }
            SignBlockEntity lv4 = (SignBlockEntity)lv3;
            lv4.tryChangeText(this.player, packet.isFront(), signText);
        }
    }

    @Override
    public void onUpdatePlayerAbilities(UpdatePlayerAbilitiesC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.getAbilities().flying = packet.isFlying() && this.player.getAbilities().allowFlying;
    }

    @Override
    public void onClientOptions(ClientOptionsC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.player.setClientOptions(packet.options());
    }

    @Override
    public void onUpdateDifficulty(UpdateDifficultyC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.player.hasPermissionLevel(2) && !this.isHost()) {
            return;
        }
        this.server.setDifficulty(packet.getDifficulty(), false);
    }

    @Override
    public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        if (!this.player.hasPermissionLevel(2) && !this.isHost()) {
            return;
        }
        this.server.setDifficultyLocked(packet.isDifficultyLocked());
    }

    @Override
    public void onPlayerSession(PlayerSessionC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        PublicPlayerSession.Serialized lv = packet.chatSession();
        PlayerPublicKey.PublicKeyData lv2 = this.session != null ? this.session.publicKeyData().data() : null;
        PlayerPublicKey.PublicKeyData lv3 = lv.publicKeyData();
        if (Objects.equals(lv2, lv3)) {
            return;
        }
        if (lv2 != null && lv3.expiresAt().isBefore(lv2.expiresAt())) {
            this.disconnect(PlayerPublicKey.EXPIRED_PUBLIC_KEY_TEXT);
            return;
        }
        try {
            SignatureVerifier lv4 = this.server.getServicesSignatureVerifier();
            if (lv4 == null) {
                LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)this.player.getGameProfile().getName());
                return;
            }
            this.setSession(lv.toSession(this.player.getGameProfile(), lv4));
        } catch (PlayerPublicKey.PublicKeyException lv5) {
            LOGGER.error("Failed to validate profile key: {}", (Object)lv5.getMessage());
            this.disconnect(lv5.getMessageText());
        }
    }

    @Override
    public void onAcknowledgeReconfiguration(AcknowledgeReconfigurationC2SPacket packet) {
        if (!this.requestedReconfiguration) {
            throw new IllegalStateException("Client acknowledged config, but none was requested");
        }
        this.connection.transitionInbound(ConfigurationStates.C2S, new ServerConfigurationNetworkHandler(this.server, this.connection, this.createClientData(this.player.getClientOptions())));
    }

    @Override
    public void onAcknowledgeChunks(AcknowledgeChunksC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.chunkDataSender.onAcknowledgeChunks(packet.desiredChunksPerTick());
    }

    @Override
    public void onDebugSampleSubscription(DebugSampleSubscriptionC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getServerWorld());
        this.server.subscribeToDebugSample(this.player, packet.sampleType());
    }

    private void setSession(PublicPlayerSession session) {
        this.session = session;
        this.messageUnpacker = session.createUnpacker(this.player.getUuid());
        this.messageChainTaskQueue.append(() -> {
            this.player.setSession(session);
            this.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.INITIALIZE_CHAT), List.of(this.player)));
        });
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) {
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @FunctionalInterface
    static interface Interaction {
        public ActionResult run(ServerPlayerEntity var1, Entity var2, Hand var3);
    }
}

