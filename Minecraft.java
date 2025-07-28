package net.minecraft.client;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.src.AchievementList;
import net.minecraft.src.AnvilSaveConverter;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.ChunkProviderLoadOrGenerate;
import net.minecraft.src.ColorizerFoliage;
import net.minecraft.src.ColorizerGrass;
import net.minecraft.src.ColorizerWater;
import net.minecraft.src.EffectRenderer;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityRenderer;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.EnumOS2;
import net.minecraft.src.EnumOSMappingHelper;
import net.minecraft.src.EnumOptions;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GameWindowListener;
import net.minecraft.src.GuiAchievement;
import net.minecraft.src.GuiChat;
import net.minecraft.src.GuiConflictWarning;
import net.minecraft.src.GuiConnecting;
import net.minecraft.src.GuiErrorScreen;
import net.minecraft.src.GuiGameOver;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.GuiIngameMenu;
import net.minecraft.src.GuiInventory;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiMemoryErrorScreen;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSleepMP;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ISaveFormat;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemRenderer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.LoadingScreenRenderer;
import net.minecraft.src.MathHelper;
import net.minecraft.src.MinecraftError;
import net.minecraft.src.MinecraftException;
import net.minecraft.src.MinecraftImpl;
import net.minecraft.src.ModelBiped;
import net.minecraft.src.MouseHelper;
import net.minecraft.src.MovementInputFromOptions;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.OpenGlCapsChecker;
import net.minecraft.src.OpenGlHelper;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.PlayerController;
import net.minecraft.src.PlayerUsageSnooper;
import net.minecraft.src.Profiler;
import net.minecraft.src.ProfilerResult;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.RenderManager;
import net.minecraft.src.ScaledResolution;
import net.minecraft.src.ScreenShotHelper;
import net.minecraft.src.Session;
import net.minecraft.src.SoundManager;
import net.minecraft.src.StatCollector;
import net.minecraft.src.StatFileWriter;
import net.minecraft.src.StatList;
import net.minecraft.src.StatStringFormatKeyInv;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.Teleporter;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TextureCompassFX;
import net.minecraft.src.TextureFlamesFX;
import net.minecraft.src.TextureLavaFX;
import net.minecraft.src.TextureLavaFlowFX;
import net.minecraft.src.TexturePackList;
import net.minecraft.src.TexturePortalFX;
import net.minecraft.src.TextureWatchFX;
import net.minecraft.src.TextureWaterFX;
import net.minecraft.src.TextureWaterFlowFX;
import net.minecraft.src.ThreadCheckHasPaid;
import net.minecraft.src.ThreadClientSleep;
import net.minecraft.src.ThreadDownloadResources;
import net.minecraft.src.Timer;
import net.minecraft.src.UnexpectedThrowable;
import net.minecraft.src.Vec3D;
import net.minecraft.src.World;
import net.minecraft.src.WorldProvider;
import net.minecraft.src.WorldRenderer;
import net.minecraft.src.WorldSettings;
import net.minecraft.src.WorldType;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import net.minecraft.src.WavBGMPlayer;
import javax.swing.JOptionPane;
import org.lwjgl.input.Keyboard;
import java.util.Random;

public abstract class Minecraft implements Runnable {
	public static byte[] field_28006_b = new byte[10485760];
	private static Minecraft theMinecraft;
	public PlayerController playerController;
	private boolean fullscreen = false;
	private boolean hasCrashed = false;
	public int displayWidth;
	public int displayHeight;
	private OpenGlCapsChecker glCapabilities;
	private Timer timer = new Timer(20.0F);
	public World theWorld;
	public RenderGlobal renderGlobal;
	public EntityPlayerSP thePlayer;
	public EntityLiving renderViewEntity;
	public EffectRenderer effectRenderer;
	public Session session = null;
	public String minecraftUri;
	public Canvas mcCanvas;
	public boolean hideQuitButton = false;
	public volatile boolean isGamePaused = false;
	public RenderEngine renderEngine;
	public FontRenderer fontRenderer;
	public FontRenderer standardGalacticFontRenderer;
	public GuiScreen currentScreen = null;
	public LoadingScreenRenderer loadingScreen;
	public EntityRenderer entityRenderer;
	private ThreadDownloadResources downloadResourcesThread;
	private int ticksRan = 0;
	private int leftClickCounter = 0;
	private int tempDisplayWidth;
	private int tempDisplayHeight;
	public GuiAchievement guiAchievement = new GuiAchievement(this);
	public GuiIngame ingameGUI;
	public boolean skipRenderWorld = false;
	public ModelBiped playerModelBiped = new ModelBiped(0.0F);
	public MovingObjectPosition objectMouseOver = null;
	public GameSettings gameSettings;
	protected MinecraftApplet mcApplet;
	public SoundManager sndManager = new SoundManager();
	public MouseHelper mouseHelper;
	public TexturePackList texturePackList;
	public File mcDataDir;
	private ISaveFormat saveLoader;
	public static long[] frameTimes = new long[512];
	public static long[] tickTimes = new long[512];
	public static int numRecordedFrameTimes = 0;
	public static long hasPaidCheckTime = 0L;
	private int rightClickDelayTimer = 0;
	public StatFileWriter statFileWriter;
	private String serverName;
	private int serverPort;
	private TextureWaterFX textureWaterFX = new TextureWaterFX();
	private TextureLavaFX textureLavaFX = new TextureLavaFX();
	private static File minecraftDir = null;
	public volatile boolean running = true;
	public String debug = "";
	private long nextPopupTime = 0;
    private Random random = new Random();
	long debugUpdateTime = System.currentTimeMillis();
	int fpsCounter = 0;
	boolean isTakingScreenshot = false;
	long prevFrameTime = -1L;
	private String debugProfilerName = "root";
	public boolean inGameHasFocus = false;
	public boolean isRaining = false;
	long systemTime = System.currentTimeMillis();
	private int joinPlayerCounter = 0;
	private WavBGMPlayer wbp = new WavBGMPlayer("/sound/bgm.wav");
	public Minecraft(Component var1, Canvas var2, MinecraftApplet var3, int var4, int var5, boolean var6) {
		StatList.func_27360_a();
		this.tempDisplayHeight = var5;
		this.fullscreen = var6;
		this.mcApplet = var3;
		Packet3Chat.field_52010_b = Short.MAX_VALUE;
		new ThreadClientSleep(this, "Timer hack thread");
		this.mcCanvas = var2;
		this.displayWidth = var4;
		this.displayHeight = var5;
		this.fullscreen = var6;
		if(var3 == null || "true".equals(var3.getParameter("stand-alone"))) {
			this.hideQuitButton = false;
		}

		theMinecraft = this;
	}

	public void onMinecraftCrash(UnexpectedThrowable var1) {
		this.hasCrashed = true;
		this.displayUnexpectedThrowable(var1);
	}

	public abstract void displayUnexpectedThrowable(UnexpectedThrowable var1);

	public void setServer(String var1, int var2) {
		this.serverName = var1;
		this.serverPort = var2;
	}

	public void startGame() throws LWJGLException {
		if(this.mcCanvas != null) {
			Graphics var1 = this.mcCanvas.getGraphics();
			if(var1 != null) {
				var1.setColor(Color.BLACK);
				var1.fillRect(0, 0, this.displayWidth, this.displayHeight);
				var1.dispose();
			}

			Display.setParent(this.mcCanvas);
		} else if(this.fullscreen) {
			Display.setFullscreen(true);
			this.displayWidth = Display.getDisplayMode().getWidth();
			this.displayHeight = Display.getDisplayMode().getHeight();
			if(this.displayWidth <= 0) {
				this.displayWidth = 1;
			}

			if(this.displayHeight <= 0) {
				this.displayHeight = 1;
			}
		} else {
			Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
		}

		Display.setTitle("Minecraft Minecraft 1.2.5");
		System.out.println("LWJGL Version: " + Sys.getVersion());

		try {
			PixelFormat var7 = new PixelFormat();
			var7 = var7.withDepthBits(24);
			Display.create(var7);
		} catch (LWJGLException var6) {
			var6.printStackTrace();

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException var5) {
			}

			Display.create();
		}

		OpenGlHelper.initializeTextures();
		this.mcDataDir = getMinecraftDir();
		this.saveLoader = new AnvilSaveConverter(new File(this.mcDataDir, "saves"));
		this.gameSettings = new GameSettings(this, this.mcDataDir);
		this.texturePackList = new TexturePackList(this, this.mcDataDir);
		this.renderEngine = new RenderEngine(this.texturePackList, this.gameSettings);
		this.loadScreen();
		this.fontRenderer = new FontRenderer(this.gameSettings, "/font/default.png", this.renderEngine, false);
		this.standardGalacticFontRenderer = new FontRenderer(this.gameSettings, "/font/alternate.png", this.renderEngine, false);
		if(this.gameSettings.language != null) {
			StringTranslate.getInstance().setLanguage(this.gameSettings.language);
			this.fontRenderer.setUnicodeFlag(StringTranslate.getInstance().isUnicode());
			this.fontRenderer.setBidiFlag(StringTranslate.isBidrectional(this.gameSettings.language));
		}

		ColorizerWater.setWaterBiomeColorizer(this.renderEngine.getTextureContents("/misc/watercolor.png"));
		ColorizerGrass.setGrassBiomeColorizer(this.renderEngine.getTextureContents("/misc/grasscolor.png"));
		ColorizerFoliage.getFoilageBiomeColorizer(this.renderEngine.getTextureContents("/misc/foliagecolor.png"));
		this.entityRenderer = new EntityRenderer(this);
		RenderManager.instance.itemRenderer = new ItemRenderer(this);
		this.statFileWriter = new StatFileWriter(this.session, this.mcDataDir);
		AchievementList.openInventory.setStatStringFormatter(new StatStringFormatKeyInv(this));
		this.loadScreen();
		Mouse.create();
		this.mouseHelper = new MouseHelper(this.mcCanvas);

		try {
			Controllers.create();
		} catch (Exception var4) {
			var4.printStackTrace();
		}

		func_52004_D();
		this.checkGLError("Pre startup");
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearDepth(1.0D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		this.checkGLError("Startup");
		this.glCapabilities = new OpenGlCapsChecker();
		this.sndManager.loadSoundSettings(this.gameSettings);
		this.renderEngine.registerTextureFX(this.textureLavaFX);
		this.renderEngine.registerTextureFX(this.textureWaterFX);
		this.renderEngine.registerTextureFX(new TexturePortalFX());
		this.renderEngine.registerTextureFX(new TextureCompassFX(this));
		this.renderEngine.registerTextureFX(new TextureWatchFX(this));
		this.renderEngine.registerTextureFX(new TextureWaterFlowFX());
		this.renderEngine.registerTextureFX(new TextureLavaFlowFX());
		this.renderEngine.registerTextureFX(new TextureFlamesFX(0));
		this.renderEngine.registerTextureFX(new TextureFlamesFX(1));
		this.renderGlobal = new RenderGlobal(this, this.renderEngine);
		GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);
		this.effectRenderer = new EffectRenderer(this.theWorld, this.renderEngine);

		try {
			this.downloadResourcesThread = new ThreadDownloadResources(this.mcDataDir, this);
			this.downloadResourcesThread.start();
		} catch (Exception var3) {
		}

		this.checkGLError("Post startup");
		this.ingameGUI = new GuiIngame(this);
		if(this.serverName != null) {
			this.displayGuiScreen(new GuiConnecting(this, this.serverName, this.serverPort));
		} else {
			this.displayGuiScreen(new GuiMainMenu());
		}

		this.loadingScreen = new LoadingScreenRenderer(this);
	}

	private void loadScreen() throws LWJGLException {
		ScaledResolution var1 = new ScaledResolution(this.gameSettings, this.displayWidth, this.displayHeight);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, var1.scaledWidthD, var1.scaledHeightD, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
		GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);
		GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		Tessellator var2 = Tessellator.instance;
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderEngine.getTexture("/title/mojang.png"));
		var2.startDrawingQuads();
		var2.setColorOpaque_I(16777215);
		var2.addVertexWithUV(0.0D, (double)this.displayHeight, 0.0D, 0.0D, 0.0D);
		var2.addVertexWithUV((double)this.displayWidth, (double)this.displayHeight, 0.0D, 0.0D, 0.0D);
		var2.addVertexWithUV((double)this.displayWidth, 0.0D, 0.0D, 0.0D, 0.0D);
		var2.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
		var2.draw();
		short var3 = 256;
		short var4 = 256;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		var2.setColorOpaque_I(16777215);
		this.scaledTessellator((var1.getScaledWidth() - var3) / 2, (var1.getScaledHeight() - var4) / 2, 0, 0, var3, var4);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		Display.swapBuffers();
	}

	public void scaledTessellator(int var1, int var2, int var3, int var4, int var5, int var6) {
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;
		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV((double)(var1 + 0), (double)(var2 + var6), 0.0D, (double)((float)(var3 + 0) * var7), (double)((float)(var4 + var6) * var8));
		var9.addVertexWithUV((double)(var1 + var5), (double)(var2 + var6), 0.0D, (double)((float)(var3 + var5) * var7), (double)((float)(var4 + var6) * var8));
		var9.addVertexWithUV((double)(var1 + var5), (double)(var2 + 0), 0.0D, (double)((float)(var3 + var5) * var7), (double)((float)(var4 + 0) * var8));
		var9.addVertexWithUV((double)(var1 + 0), (double)(var2 + 0), 0.0D, (double)((float)(var3 + 0) * var7), (double)((float)(var4 + 0) * var8));
		var9.draw();
	}

	public static File getMinecraftDir() {
		if(minecraftDir == null) {
			minecraftDir = getAppDir("minecraft");
		}

		return minecraftDir;
	}

	public static File getAppDir(String var0) {
		String var1 = System.getProperty("user.home", ".");
		File var2;
		switch(EnumOSMappingHelper.enumOSMappingArray[getOs().ordinal()]) {
		case 1:
		case 2:
			var2 = new File(var1, '.' + var0 + '/');
			break;
		case 3:
			String var3 = System.getenv("APPDATA");
			if(var3 != null) {
				var2 = new File(var3, "." + var0 + '/');
			} else {
				var2 = new File(var1, '.' + var0 + '/');
			}
			break;
		case 4:
			var2 = new File(var1, "Library/Application Support/" + var0);
			break;
		default:
			var2 = new File(var1, var0 + '/');
		}

		if(!var2.exists() && !var2.mkdirs()) {
			throw new RuntimeException("The working directory could not be created: " + var2);
		} else {
			return var2;
		}
	}

	private static EnumOS2 getOs() {
		String var0 = System.getProperty("os.name").toLowerCase();
		return var0.contains("win") ? EnumOS2.windows : (var0.contains("mac") ? EnumOS2.macos : (var0.contains("solaris") ? EnumOS2.solaris : (var0.contains("sunos") ? EnumOS2.solaris : (var0.contains("linux") ? EnumOS2.linux : (var0.contains("unix") ? EnumOS2.linux : EnumOS2.unknown)))));
	}

	public ISaveFormat getSaveLoader() {
		return this.saveLoader;
	}

	public void displayGuiScreen(GuiScreen var1) {
		if(!(this.currentScreen instanceof GuiErrorScreen)) {
			if(this.currentScreen != null) {
				this.currentScreen.onGuiClosed();
			}

			if(var1 instanceof GuiMainMenu) {
				this.statFileWriter.func_27175_b();
			}

			this.statFileWriter.syncStats();
			if(var1 == null && this.theWorld == null) {
				var1 = new GuiMainMenu();
			} else if(var1 == null && this.thePlayer.getHealth() <= 0) {
				var1 = new GuiGameOver();
			}

			if(var1 instanceof GuiMainMenu) {
				this.gameSettings.showDebugInfo = false;
				this.ingameGUI.clearChatMessages();
			}

			this.currentScreen = (GuiScreen)var1;
			if(var1 != null) {
				this.setIngameNotInFocus();
				ScaledResolution var2 = new ScaledResolution(this.gameSettings, this.displayWidth, this.displayHeight);
				int var3 = var2.getScaledWidth();
				int var4 = var2.getScaledHeight();
				((GuiScreen)var1).setWorldAndResolution(this, var3, var4);
				this.skipRenderWorld = false;
			} else {
				this.setIngameFocus();
			}

		}
	}

	private void checkGLError(String var1) {
		int var2 = GL11.glGetError();
		if(var2 != 0) {
			String var3 = GLU.gluErrorString(var2);
			System.out.println("########## GL ERROR ##########");
			System.out.println("@ " + var1);
			System.out.println(var2 + ": " + var3);
		}

	}

	public void shutdownMinecraftApplet() {
		try {
			this.statFileWriter.func_27175_b();
			this.statFileWriter.syncStats();
			if(this.mcApplet != null) {
				this.mcApplet.clearApplet();
			}

			try {
				if(this.downloadResourcesThread != null) {
					this.downloadResourcesThread.closeMinecraft();
				}
			} catch (Exception var9) {
			}

			System.out.println("Stopping!");

			try {
				this.changeWorld1((World)null);
			} catch (Throwable var8) {
			}

			try {
				GLAllocation.deleteTexturesAndDisplayLists();
			} catch (Throwable var7) {
			}

			this.sndManager.closeMinecraft();
			Mouse.destroy();
			Keyboard.destroy();
		} finally {
			Display.destroy();
			if(!this.hasCrashed) {
				System.exit(0);
			}

		}

		System.gc();
	}

	public void run() {
		this.running = true;

		try {
			this.startGame();
		} catch (Exception var11) {
			var11.printStackTrace();
			this.onMinecraftCrash(new UnexpectedThrowable("Failed to start game", var11));
			return;
		}

		try {
			while(this.running) {
				try {
					this.runGameLoop();
				} catch (MinecraftException var9) {
					this.theWorld = null;
					this.changeWorld1((World)null);
					this.displayGuiScreen(new GuiConflictWarning());
				} catch (OutOfMemoryError var10) {
					this.freeMemory();
					this.displayGuiScreen(new GuiMemoryErrorScreen());
					System.gc();
				}
			}
		} catch (MinecraftError var12) {
		} catch (Throwable var13) {
			this.freeMemory();
			var13.printStackTrace();
			this.onMinecraftCrash(new UnexpectedThrowable("Unexpected error", var13));
		} finally {
			this.shutdownMinecraftApplet();
		}

	}

	private void runGameLoop() {
		if(this.mcApplet != null && !this.mcApplet.isActive()) {
			this.running = false;
		} else {
			AxisAlignedBB.clearBoundingBoxPool();
			Vec3D.initialize();
			Profiler.startSection("root");
			if(this.mcCanvas == null && Display.isCloseRequested()) {
				this.shutdown();
			}

			if(this.isGamePaused && this.theWorld != null) {
				float var1 = this.timer.renderPartialTicks;
				this.timer.updateTimer();
				this.timer.renderPartialTicks = var1;
			} else {
				this.timer.updateTimer();
			}

			long var6 = System.nanoTime();
			Profiler.startSection("tick");

			for(int var3 = 0; var3 < this.timer.elapsedTicks; ++var3) {
				++this.ticksRan;

				try {
					this.runTick();
				} catch (MinecraftException var5) {
					this.theWorld = null;
					this.changeWorld1((World)null);
					this.displayGuiScreen(new GuiConflictWarning());
				}
			}

			Profiler.endSection();
			long var7 = System.nanoTime() - var6;
			this.checkGLError("Pre render");
			RenderBlocks.fancyGrass = this.gameSettings.fancyGraphics;
			Profiler.startSection("sound");
			this.sndManager.setListener(this.thePlayer, this.timer.renderPartialTicks);
			Profiler.endStartSection("updatelights");
			if (this.theWorld != null) {
				this.theWorld.updatingLighting();
				this.wbp.stop();
			} else {
				this.wbp.playLoop();
			}
			


			Profiler.endSection();
			Profiler.startSection("render");
			Profiler.startSection("display");
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			if(!Keyboard.isKeyDown(Keyboard.KEY_F7)) {
				Display.update();
			}

			if(this.thePlayer != null && this.thePlayer.isEntityInsideOpaqueBlock()) {
				this.gameSettings.thirdPersonView = 0;
			}

			Profiler.endSection();
			if(!this.skipRenderWorld) {
				Profiler.startSection("gameMode");
				if(this.playerController != null) {
					this.playerController.setPartialTime(this.timer.renderPartialTicks);
				}

				Profiler.endStartSection("gameRenderer");
				this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks);
				Profiler.endSection();
			}

			GL11.glFlush();
			Profiler.endSection();
			if(!Display.isActive() && this.fullscreen) {
				this.toggleFullscreen();
			}

			Profiler.endSection();
			if(this.gameSettings.showDebugInfo && this.gameSettings.field_50119_G) {
				if(!Profiler.profilingEnabled) {
					Profiler.clearProfiling();
				}

				Profiler.profilingEnabled = true;
				this.displayDebugInfo(var7);
			} else {
				Profiler.profilingEnabled = false;
				this.prevFrameTime = System.nanoTime();
			}

			this.guiAchievement.updateAchievementWindow();
			Profiler.startSection("root");
			Thread.yield();
			if(Keyboard.isKeyDown(Keyboard.KEY_F7)) {
				Display.update();
			}

			this.screenshotListener();
			if(this.mcCanvas != null && !this.fullscreen && (this.mcCanvas.getWidth() != this.displayWidth || this.mcCanvas.getHeight() != this.displayHeight)) {
				this.displayWidth = this.mcCanvas.getWidth();
				this.displayHeight = this.mcCanvas.getHeight();
				if(this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if(this.displayHeight <= 0) {
					this.displayHeight = 1;
				}

				this.resize(this.displayWidth, this.displayHeight);
			}

			this.checkGLError("Post render");
			++this.fpsCounter;

			for(this.isGamePaused = !this.isMultiplayerWorld() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame(); System.currentTimeMillis() >= this.debugUpdateTime + 1000L; this.fpsCounter = 0) {
				this.debug = this.fpsCounter + " fps, " + WorldRenderer.chunksUpdated + " chunk updates";
				WorldRenderer.chunksUpdated = 0;
				this.debugUpdateTime += 1000L;
			}

			Profiler.endSection();
		}
	}

	public void freeMemory() {
		try {
			field_28006_b = new byte[0];
			this.renderGlobal.func_28137_f();
		} catch (Throwable var4) {
		}

		try {
			System.gc();
			AxisAlignedBB.clearBoundingBoxes();
			Vec3D.clearVectorList();
		} catch (Throwable var3) {
		}

		try {
			System.gc();
			this.changeWorld1((World)null);
		} catch (Throwable var2) {
		}

		System.gc();
	}

	private void screenshotListener() {
		if(Keyboard.isKeyDown(Keyboard.KEY_F2)) {
			if(!this.isTakingScreenshot) {
				this.isTakingScreenshot = true;
				this.ingameGUI.addChatMessage(ScreenShotHelper.saveScreenshot(minecraftDir, this.displayWidth, this.displayHeight));
			}
		} else {
			this.isTakingScreenshot = false;
		}

	}

	private void updateDebugProfilerName(int var1) {
		List var2 = Profiler.getProfilingData(this.debugProfilerName);
		if(var2 != null && var2.size() != 0) {
			ProfilerResult var3 = (ProfilerResult)var2.remove(0);
			if(var1 == 0) {
				if(var3.name.length() > 0) {
					int var4 = this.debugProfilerName.lastIndexOf(".");
					if(var4 >= 0) {
						this.debugProfilerName = this.debugProfilerName.substring(0, var4);
					}
				}
			} else {
				--var1;
				if(var1 < var2.size() && !((ProfilerResult)var2.get(var1)).name.equals("unspecified")) {
					if(this.debugProfilerName.length() > 0) {
						this.debugProfilerName = this.debugProfilerName + ".";
					}

					this.debugProfilerName = this.debugProfilerName + ((ProfilerResult)var2.get(var1)).name;
				}
			}

		}
	}

	private void displayDebugInfo(long var1) {
		List var3 = Profiler.getProfilingData(this.debugProfilerName);
		ProfilerResult var4 = (ProfilerResult)var3.remove(0);
		long var5 = 16666666L;
		if(this.prevFrameTime == -1L) {
			this.prevFrameTime = System.nanoTime();
		}

		long var7 = System.nanoTime();
		tickTimes[numRecordedFrameTimes & frameTimes.length - 1] = var1;
		frameTimes[numRecordedFrameTimes++ & frameTimes.length - 1] = var7 - this.prevFrameTime;
		this.prevFrameTime = var7;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)this.displayWidth, (double)this.displayHeight, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
		GL11.glLineWidth(1.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator var9 = Tessellator.instance;
		var9.startDrawing(7);
		int var10 = (int)(var5 / 200000L);
		var9.setColorOpaque_I(536870912);
		var9.addVertex(0.0D, (double)(this.displayHeight - var10), 0.0D);
		var9.addVertex(0.0D, (double)this.displayHeight, 0.0D);
		var9.addVertex((double)frameTimes.length, (double)this.displayHeight, 0.0D);
		var9.addVertex((double)frameTimes.length, (double)(this.displayHeight - var10), 0.0D);
		var9.setColorOpaque_I(538968064);
		var9.addVertex(0.0D, (double)(this.displayHeight - var10 * 2), 0.0D);
		var9.addVertex(0.0D, (double)(this.displayHeight - var10), 0.0D);
		var9.addVertex((double)frameTimes.length, (double)(this.displayHeight - var10), 0.0D);
		var9.addVertex((double)frameTimes.length, (double)(this.displayHeight - var10 * 2), 0.0D);
		var9.draw();
		long var11 = 0L;

		int var13;
		for(var13 = 0; var13 < frameTimes.length; ++var13) {
			var11 += frameTimes[var13];
		}

		var13 = (int)(var11 / 200000L / (long)frameTimes.length);
		var9.startDrawing(7);
		var9.setColorOpaque_I(541065216);
		var9.addVertex(0.0D, (double)(this.displayHeight - var13), 0.0D);
		var9.addVertex(0.0D, (double)this.displayHeight, 0.0D);
		var9.addVertex((double)frameTimes.length, (double)this.displayHeight, 0.0D);
		var9.addVertex((double)frameTimes.length, (double)(this.displayHeight - var13), 0.0D);
		var9.draw();
		var9.startDrawing(1);

		int var15;
		int var16;
		for(int var14 = 0; var14 < frameTimes.length; ++var14) {
			var15 = (var14 - numRecordedFrameTimes & frameTimes.length - 1) * 255 / frameTimes.length;
			var16 = var15 * var15 / 255;
			var16 = var16 * var16 / 255;
			int var17 = var16 * var16 / 255;
			var17 = var17 * var17 / 255;
			if(frameTimes[var14] > var5) {
				var9.setColorOpaque_I(-16777216 + var16 * 65536);
			} else {
				var9.setColorOpaque_I(-16777216 + var16 * 256);
			}

			long var18 = frameTimes[var14] / 200000L;
			long var20 = tickTimes[var14] / 200000L;
			var9.addVertex((double)((float)var14 + 0.5F), (double)((float)((long)this.displayHeight - var18) + 0.5F), 0.0D);
			var9.addVertex((double)((float)var14 + 0.5F), (double)((float)this.displayHeight + 0.5F), 0.0D);
			var9.setColorOpaque_I(-16777216 + var16 * 65536 + var16 * 256 + var16 * 1);
			var9.addVertex((double)((float)var14 + 0.5F), (double)((float)((long)this.displayHeight - var18) + 0.5F), 0.0D);
			var9.addVertex((double)((float)var14 + 0.5F), (double)((float)((long)this.displayHeight - (var18 - var20)) + 0.5F), 0.0D);
		}

		var9.draw();
		short var26 = 160;
		var15 = this.displayWidth - var26 - 10;
		var16 = this.displayHeight - var26 * 2;
		GL11.glEnable(GL11.GL_BLEND);
		var9.startDrawingQuads();
		var9.setColorRGBA_I(0, 200);
		var9.addVertex((double)((float)var15 - (float)var26 * 1.1F), (double)((float)var16 - (float)var26 * 0.6F - 16.0F), 0.0D);
		var9.addVertex((double)((float)var15 - (float)var26 * 1.1F), (double)(var16 + var26 * 2), 0.0D);
		var9.addVertex((double)((float)var15 + (float)var26 * 1.1F), (double)(var16 + var26 * 2), 0.0D);
		var9.addVertex((double)((float)var15 + (float)var26 * 1.1F), (double)((float)var16 - (float)var26 * 0.6F - 16.0F), 0.0D);
		var9.draw();
		GL11.glDisable(GL11.GL_BLEND);
		double var27 = 0.0D;

		int var21;
		for(int var19 = 0; var19 < var3.size(); ++var19) {
			ProfilerResult var29 = (ProfilerResult)var3.get(var19);
			var21 = MathHelper.floor_double(var29.sectionPercentage / 4.0D) + 1;
			var9.startDrawing(6);
			var9.setColorOpaque_I(var29.getDisplayColor());
			var9.addVertex((double)var15, (double)var16, 0.0D);

			int var22;
			float var23;
			float var24;
			float var25;
			for(var22 = var21; var22 >= 0; --var22) {
				var23 = (float)((var27 + var29.sectionPercentage * (double)var22 / (double)var21) * (double)((float)Math.PI) * 2.0D / 100.0D);
				var24 = MathHelper.sin(var23) * (float)var26;
				var25 = MathHelper.cos(var23) * (float)var26 * 0.5F;
				var9.addVertex((double)((float)var15 + var24), (double)((float)var16 - var25), 0.0D);
			}

			var9.draw();
			var9.startDrawing(5);
			var9.setColorOpaque_I((var29.getDisplayColor() & 16711422) >> 1);

			for(var22 = var21; var22 >= 0; --var22) {
				var23 = (float)((var27 + var29.sectionPercentage * (double)var22 / (double)var21) * (double)((float)Math.PI) * 2.0D / 100.0D);
				var24 = MathHelper.sin(var23) * (float)var26;
				var25 = MathHelper.cos(var23) * (float)var26 * 0.5F;
				var9.addVertex((double)((float)var15 + var24), (double)((float)var16 - var25), 0.0D);
				var9.addVertex((double)((float)var15 + var24), (double)((float)var16 - var25 + 10.0F), 0.0D);
			}

			var9.draw();
			var27 += var29.sectionPercentage;
		}

		DecimalFormat var28 = new DecimalFormat("##0.00");
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		String var30 = "";
		if(!var4.name.equals("unspecified")) {
			var30 = var30 + "[0] ";
		}

		if(var4.name.length() == 0) {
			var30 = var30 + "ROOT ";
		} else {
			var30 = var30 + var4.name + " ";
		}

		var21 = 16777215;
		this.fontRenderer.drawStringWithShadow(var30, var15 - var26, var16 - var26 / 2 - 16, var21);
		FontRenderer var10000 = this.fontRenderer;
		var30 = var28.format(var4.globalPercentage) + "%";
		var10000.drawStringWithShadow(var30, var15 + var26 - this.fontRenderer.getStringWidth(var30), var16 - var26 / 2 - 16, var21);

		for(int var32 = 0; var32 < var3.size(); ++var32) {
			ProfilerResult var31 = (ProfilerResult)var3.get(var32);
			String var33 = "";
			if(!var31.name.equals("unspecified")) {
				var33 = var33 + "[" + (var32 + 1) + "] ";
			} else {
				var33 = var33 + "[?] ";
			}

			var33 = var33 + var31.name;
			this.fontRenderer.drawStringWithShadow(var33, var15 - var26, var16 + var26 / 2 + var32 * 8 + 20, var31.getDisplayColor());
			var10000 = this.fontRenderer;
			var33 = var28.format(var31.sectionPercentage) + "%";
			var10000.drawStringWithShadow(var33, var15 + var26 - 50 - this.fontRenderer.getStringWidth(var33), var16 + var26 / 2 + var32 * 8 + 20, var31.getDisplayColor());
			var10000 = this.fontRenderer;
			var33 = var28.format(var31.globalPercentage) + "%";
			var10000.drawStringWithShadow(var33, var15 + var26 - this.fontRenderer.getStringWidth(var33), var16 + var26 / 2 + var32 * 8 + 20, var31.getDisplayColor());
		}

	}

	public void shutdown() {
		this.running = false;
	}

	public void setIngameFocus() {
		if(Display.isActive()) {
			if(!this.inGameHasFocus) {
				this.inGameHasFocus = true;
				this.mouseHelper.grabMouseCursor();
				this.displayGuiScreen((GuiScreen)null);
				this.leftClickCounter = 10000;
			}
		}
	}

	public void setIngameNotInFocus() {
		if(this.inGameHasFocus) {
			KeyBinding.unPressAllKeys();
			this.inGameHasFocus = false;
			this.mouseHelper.ungrabMouseCursor();
		}
	}

	public void displayInGameMenu() {
		if(this.currentScreen == null) {
			this.displayGuiScreen(new GuiIngameMenu());
		}
	}

	private void sendClickBlockToController(int var1, boolean var2) {
		if(!var2) {
			this.leftClickCounter = 0;
		}

		if(var1 != 0 || this.leftClickCounter <= 0) {
			if(var2 && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE && var1 == 0) {
				int var3 = this.objectMouseOver.blockX;
				int var4 = this.objectMouseOver.blockY;
				int var5 = this.objectMouseOver.blockZ;
				this.playerController.onPlayerDamageBlock(var3, var4, var5, this.objectMouseOver.sideHit);
				if(this.thePlayer.canPlayerEdit(var3, var4, var5)) {
					this.effectRenderer.addBlockHitEffects(var3, var4, var5, this.objectMouseOver.sideHit);
					this.thePlayer.swingItem();
				}
			} else {
				this.playerController.resetBlockRemoving();
			}

		}
	}

	private void clickMouse(int var1) {
		if(var1 != 0 || this.leftClickCounter <= 0) {
			if(var1 == 0) {
				this.thePlayer.swingItem();
			}

			if(var1 == 1) {
				this.rightClickDelayTimer = 4;
			}

			boolean var2 = true;
			ItemStack var3 = this.thePlayer.inventory.getCurrentItem();
			if(this.objectMouseOver == null) {
				if(var1 == 0 && this.playerController.isNotCreative()) {
					this.leftClickCounter = 10;
				}
			} else if(this.objectMouseOver.typeOfHit == EnumMovingObjectType.ENTITY) {
				if(var1 == 0) {
					this.playerController.attackEntity(this.thePlayer, this.objectMouseOver.entityHit);
				}

				if(var1 == 1) {
					this.playerController.interactWithEntity(this.thePlayer, this.objectMouseOver.entityHit);
				}
			} else if(this.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
				int var4 = this.objectMouseOver.blockX;
				int var5 = this.objectMouseOver.blockY;
				int var6 = this.objectMouseOver.blockZ;
				int var7 = this.objectMouseOver.sideHit;
				if(var1 == 0) {
					this.playerController.clickBlock(var4, var5, var6, this.objectMouseOver.sideHit);
				} else {
					int var9 = var3 != null ? var3.stackSize : 0;
					if(this.playerController.onPlayerRightClick(this.thePlayer, this.theWorld, var3, var4, var5, var6, var7)) {
						var2 = false;
						this.thePlayer.swingItem();
					}

					if(var3 == null) {
						return;
					}

					if(var3.stackSize == 0) {
						this.thePlayer.inventory.mainInventory[this.thePlayer.inventory.currentItem] = null;
					} else if(var3.stackSize != var9 || this.playerController.isInCreativeMode()) {
						this.entityRenderer.itemRenderer.func_9449_b();
					}
				}
			}

			if(var2 && var1 == 1) {
				ItemStack var10 = this.thePlayer.inventory.getCurrentItem();
				if(var10 != null && this.playerController.sendUseItem(this.thePlayer, this.theWorld, var10)) {
					this.entityRenderer.itemRenderer.func_9450_c();
				}
			}

		}
	}

	public void toggleFullscreen() {
		try {
			this.fullscreen = !this.fullscreen;
			if(this.fullscreen) {
				Display.setDisplayMode(Display.getDesktopDisplayMode());
				this.displayWidth = Display.getDisplayMode().getWidth();
				this.displayHeight = Display.getDisplayMode().getHeight();
				if(this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if(this.displayHeight <= 0) {
					this.displayHeight = 1;
				}
			} else {
				if(this.mcCanvas != null) {
					this.displayWidth = this.mcCanvas.getWidth();
					this.displayHeight = this.mcCanvas.getHeight();
				} else {
					this.displayWidth = this.tempDisplayWidth;
					this.displayHeight = this.tempDisplayHeight;
				}

				if(this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if(this.displayHeight <= 0) {
					this.displayHeight = 1;
				}
			}

			if(this.currentScreen != null) {
				this.resize(this.displayWidth, this.displayHeight);
			}

			Display.setFullscreen(this.fullscreen);
			Display.update();
		} catch (Exception var2) {
			var2.printStackTrace();
		}

	}

	private void resize(int var1, int var2) {
		if(var1 <= 0) {
			var1 = 1;
		}

		if(var2 <= 0) {
			var2 = 1;
		}

		this.displayWidth = var1;
		this.displayHeight = var2;
		if(this.currentScreen != null) {
			ScaledResolution var3 = new ScaledResolution(this.gameSettings, var1, var2);
			int var4 = var3.getScaledWidth();
			int var5 = var3.getScaledHeight();
			this.currentScreen.setWorldAndResolution(this, var4, var5);
		}

	}

	private void startThreadCheckHasPaid() {
		(new ThreadCheckHasPaid(this)).start();
	}

	public void runTick() {
		if(this.rightClickDelayTimer > 0) {
			--this.rightClickDelayTimer;
		}

		if(this.ticksRan == 6000) {
			this.startThreadCheckHasPaid();
		}

		Profiler.startSection("stats");
		this.statFileWriter.func_27178_d();
		Profiler.endStartSection("gui");
		if(!this.isGamePaused) {
			this.ingameGUI.updateTick();
		}
Random rand = new Random();
        if (rand.nextInt(1000) == 0) { 
            JOptionPane.showMessageDialog(null, "I curse you.", "Warning.", JOptionPane.WARNING_MESSAGE);
        }
		Profiler.endStartSection("pick");
		this.entityRenderer.getMouseOver(1.0F);
		Profiler.endStartSection("centerChunkSource");
		int var3;
		if(this.thePlayer != null) {
			IChunkProvider var1 = this.theWorld.getChunkProvider();
			if(var1 instanceof ChunkProviderLoadOrGenerate) {
				ChunkProviderLoadOrGenerate var2 = (ChunkProviderLoadOrGenerate)var1;
				var3 = MathHelper.floor_float((float)((int)this.thePlayer.posX)) >> 4;
				int var4 = MathHelper.floor_float((float)((int)this.thePlayer.posZ)) >> 4;
				var2.setCurrentChunkOver(var3, var4);
			}
		}

		Profiler.endStartSection("gameMode");
		if(!this.isGamePaused && this.theWorld != null) {
			this.playerController.updateController();
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderEngine.getTexture("/terrain.png"));
		Profiler.endStartSection("textures");
		if(!this.isGamePaused) {
			this.renderEngine.updateDynamicTextures();
		}

		if(this.currentScreen == null && this.thePlayer != null) {
			if(this.thePlayer.getHealth() <= 0) {
				this.displayGuiScreen((GuiScreen)null);
			} else if(this.thePlayer.isPlayerSleeping() && this.theWorld != null && this.theWorld.isRemote) {
				this.displayGuiScreen(new GuiSleepMP());
			}
		} else if(this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.thePlayer.isPlayerSleeping()) {
			this.displayGuiScreen((GuiScreen)null);
		}

		if(this.currentScreen != null) {
			this.leftClickCounter = 10000;
		}

		if(this.currentScreen != null) {
			this.currentScreen.handleInput();
			if(this.currentScreen != null) {
				this.currentScreen.guiParticles.update();
				this.currentScreen.updateScreen();
			}
		}

		if(this.currentScreen == null || this.currentScreen.allowUserInput) {
			Profiler.endStartSection("mouse");

			while(Mouse.next()) {
				KeyBinding.setKeyBindState(Mouse.getEventButton() - 100, Mouse.getEventButtonState());
				if(Mouse.getEventButtonState()) {
					KeyBinding.onTick(Mouse.getEventButton() - 100);
				}

				long var5 = System.currentTimeMillis() - this.systemTime;
				if(var5 <= 200L) {
					var3 = Mouse.getEventDWheel();
					if(var3 != 0) {
						this.thePlayer.inventory.changeCurrentItem(var3);
						if(this.gameSettings.noclip) {
							if(var3 > 0) {
								var3 = 1;
							}

							if(var3 < 0) {
								var3 = -1;
							}

							this.gameSettings.noclipRate += (float)var3 * 0.25F;
						}
					}

					if(this.currentScreen == null) {
						if(!this.inGameHasFocus && Mouse.getEventButtonState()) {
							this.setIngameFocus();
						}
					} else if(this.currentScreen != null) {
						this.currentScreen.handleMouseInput();
					}
				}
			}

			if(this.leftClickCounter > 0) {
				--this.leftClickCounter;
			}

			Profiler.endStartSection("keyboard");

			label361:
			while(true) {
				while(true) {
					do {
						if(!Keyboard.next()) {
							while(this.gameSettings.keyBindInventory.isPressed()) {
								this.displayGuiScreen(new GuiInventory(this.thePlayer));
							}

							while(this.gameSettings.keyBindDrop.isPressed()) {
								this.thePlayer.dropOneItem();
							}

							while(this.isMultiplayerWorld() && this.gameSettings.keyBindChat.isPressed()) {
								this.displayGuiScreen(new GuiChat());
							}

							if(this.isMultiplayerWorld() && this.currentScreen == null && (Keyboard.isKeyDown(Keyboard.KEY_SLASH) || Keyboard.isKeyDown(Keyboard.KEY_DIVIDE))) {
								this.displayGuiScreen(new GuiChat("/"));
							}

							if(this.thePlayer.isUsingItem()) {
								if(!this.gameSettings.keyBindUseItem.pressed) {
									this.playerController.onStoppedUsingItem(this.thePlayer);
								}

								while(true) {
									if(!this.gameSettings.keyBindAttack.isPressed()) {
										while(this.gameSettings.keyBindUseItem.isPressed()) {
										}

										while(this.gameSettings.keyBindPickBlock.isPressed()) {
										}
										break;
									}
								}
							} else {
								while(this.gameSettings.keyBindAttack.isPressed()) {
									this.clickMouse(0);
								}

								while(this.gameSettings.keyBindUseItem.isPressed()) {
									this.clickMouse(1);
								}

								while(this.gameSettings.keyBindPickBlock.isPressed()) {
									this.clickMiddleMouseButton();
								}
							}

							if(this.gameSettings.keyBindUseItem.pressed && this.rightClickDelayTimer == 0 && !this.thePlayer.isUsingItem()) {
								this.clickMouse(1);
							}

							this.sendClickBlockToController(0, this.currentScreen == null && this.gameSettings.keyBindAttack.pressed && this.inGameHasFocus);
							break label361;
						}

						KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
						if(Keyboard.getEventKeyState()) {
							KeyBinding.onTick(Keyboard.getEventKey());
						}
					} while(!Keyboard.getEventKeyState());

					if(Keyboard.getEventKey() == Keyboard.KEY_F11) {
						this.toggleFullscreen();
					} else {
						if(this.currentScreen != null) {
							this.currentScreen.handleKeyboardInput();
						} else {
							if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
								this.displayInGameMenu();
							}

							if(Keyboard.getEventKey() == Keyboard.KEY_S && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
								this.forceReload();
							}

							if(Keyboard.getEventKey() == Keyboard.KEY_T && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
								this.renderEngine.refreshTextures();
							}

							if(Keyboard.getEventKey() == Keyboard.KEY_F && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
								boolean var6 = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) | Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
								this.gameSettings.setOptionValue(EnumOptions.RENDER_DISTANCE, var6 ? -1 : 1);
							}

							if(Keyboard.getEventKey() == Keyboard.KEY_A && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
								this.renderGlobal.loadRenderers();
							}

							if(Keyboard.getEventKey() == Keyboard.KEY_F1) {
								this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
							}

							if(Keyboard.getEventKey() == Keyboard.KEY_F3) {
								this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
								this.gameSettings.field_50119_G = !GuiScreen.func_50049_m();
							}

							if(Keyboard.getEventKey() == Keyboard.KEY_F5) {
								++this.gameSettings.thirdPersonView;
								if(this.gameSettings.thirdPersonView > 2) {
									this.gameSettings.thirdPersonView = 0;
								}
							}

							if(Keyboard.getEventKey() == Keyboard.KEY_F8) {
								this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
							}
						}

						int var7;
						for(var7 = 0; var7 < 9; ++var7) {
							if(Keyboard.getEventKey() == Keyboard.KEY_1 + var7) {
								this.thePlayer.inventory.currentItem = var7;
							}
						}

						if(this.gameSettings.showDebugInfo && this.gameSettings.field_50119_G) {
							if(Keyboard.getEventKey() == Keyboard.KEY_0) {
								this.updateDebugProfilerName(0);
							}

							for(var7 = 0; var7 < 9; ++var7) {
								if(Keyboard.getEventKey() == Keyboard.KEY_1 + var7) {
									this.updateDebugProfilerName(var7 + 1);
								}
							}
						}
					}
				}
			}
		}

		if(this.theWorld != null) {
			if(this.thePlayer != null) {
				++this.joinPlayerCounter;
				if(this.joinPlayerCounter == 30) {
					this.joinPlayerCounter = 0;
					this.theWorld.joinEntityInSurroundings(this.thePlayer);
				}
			}

			if(this.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
				this.theWorld.difficultySetting = 3;
			} else {
				this.theWorld.difficultySetting = this.gameSettings.difficulty;
			}

			if(this.theWorld.isRemote) {
				this.theWorld.difficultySetting = 1;
			}

			Profiler.endStartSection("gameRenderer");
			if(!this.isGamePaused) {
				this.entityRenderer.updateRenderer();
			}

			Profiler.endStartSection("levelRenderer");
			if(!this.isGamePaused) {
				this.renderGlobal.updateClouds();
			}

			Profiler.endStartSection("level");
			if(!this.isGamePaused) {
				if(this.theWorld.lightningFlash > 0) {
					--this.theWorld.lightningFlash;
				}

				this.theWorld.updateEntities();
			}

			if(!this.isGamePaused || this.isMultiplayerWorld()) {
				this.theWorld.setAllowedSpawnTypes(this.theWorld.difficultySetting > 0, true);
				this.theWorld.tick();
			}

			Profiler.endStartSection("animateTick");
			if(!this.isGamePaused && this.theWorld != null) {
				this.theWorld.randomDisplayUpdates(MathHelper.floor_double(this.thePlayer.posX), MathHelper.floor_double(this.thePlayer.posY), MathHelper.floor_double(this.thePlayer.posZ));
			}

			Profiler.endStartSection("particles");
			if(!this.isGamePaused) {
				this.effectRenderer.updateEffects();
			}
		}

		Profiler.endSection();
		this.systemTime = System.currentTimeMillis();
	}

	private void forceReload() {
		System.out.println("FORCING RELOAD!");
		this.sndManager = new SoundManager();
		this.sndManager.loadSoundSettings(this.gameSettings);
		this.downloadResourcesThread.reloadResources();
	}

	public boolean isMultiplayerWorld() {
		return this.theWorld != null && this.theWorld.isRemote;
	}

	public void startWorld(String var1, String var2, WorldSettings var3) {
		this.changeWorld1((World)null);
		System.gc();
		if(this.saveLoader.isOldMapFormat(var1)) {
			this.convertMapFormat(var1, var2);
		} else {
			if(this.loadingScreen != null) {
				this.loadingScreen.printText(StatCollector.translateToLocal("menu.switchingLevel"));
				this.loadingScreen.displayLoadingString("");
			}

			ISaveHandler var4 = this.saveLoader.getSaveLoader(var1, false);
			World var5 = null;
			var5 = new World(var4, var2, var3);
			if(var5.isNewWorld) {
				this.statFileWriter.readStat(StatList.createWorldStat, 1);
				this.statFileWriter.readStat(StatList.startGameStat, 1);
				this.changeWorld2(var5, StatCollector.translateToLocal("menu.generatingLevel"));
			} else {
				this.statFileWriter.readStat(StatList.loadWorldStat, 1);
				this.statFileWriter.readStat(StatList.startGameStat, 1);
				this.changeWorld2(var5, StatCollector.translateToLocal("menu.loadingLevel"));
			}
		}

	}

	public void usePortal(int var1) {
		int var2 = this.thePlayer.dimension;
		this.thePlayer.dimension = var1;
		this.theWorld.setEntityDead(this.thePlayer);
		this.thePlayer.isDead = false;
		double var3 = this.thePlayer.posX;
		double var5 = this.thePlayer.posZ;
		double var7 = 1.0D;
		if(var2 > -1 && this.thePlayer.dimension == -1) {
			var7 = 0.125D;
		} else if(var2 == -1 && this.thePlayer.dimension > -1) {
			var7 = 8.0D;
		}

		var3 *= var7;
		var5 *= var7;
		World var9;
		if(this.thePlayer.dimension == -1) {
			this.thePlayer.setLocationAndAngles(var3, this.thePlayer.posY, var5, this.thePlayer.rotationYaw, this.thePlayer.rotationPitch);
			if(this.thePlayer.isEntityAlive()) {
				this.theWorld.updateEntityWithOptionalForce(this.thePlayer, false);
			}

			var9 = null;
			var9 = new World(this.theWorld, WorldProvider.getProviderForDimension(this.thePlayer.dimension));
			this.changeWorld(var9, "Entering the Nether", this.thePlayer);
		} else if(this.thePlayer.dimension == 0) {
			if(this.thePlayer.isEntityAlive()) {
				this.thePlayer.setLocationAndAngles(var3, this.thePlayer.posY, var5, this.thePlayer.rotationYaw, this.thePlayer.rotationPitch);
				this.theWorld.updateEntityWithOptionalForce(this.thePlayer, false);
			}

			var9 = null;
			var9 = new World(this.theWorld, WorldProvider.getProviderForDimension(this.thePlayer.dimension));
			if(var2 == -1) {
				this.changeWorld(var9, "Leaving the Nether", this.thePlayer);
			} else {
				this.changeWorld(var9, "Leaving the End", this.thePlayer);
			}
		} else {
			var9 = null;
			var9 = new World(this.theWorld, WorldProvider.getProviderForDimension(this.thePlayer.dimension));
			ChunkCoordinates var10 = var9.getEntrancePortalLocation();
			var3 = (double)var10.posX;
			this.thePlayer.posY = (double)var10.posY;
			var5 = (double)var10.posZ;
			this.thePlayer.setLocationAndAngles(var3, this.thePlayer.posY, var5, 90.0F, 0.0F);
			if(this.thePlayer.isEntityAlive()) {
				var9.updateEntityWithOptionalForce(this.thePlayer, false);
			}

			this.changeWorld(var9, "Entering the End", this.thePlayer);
		}

		this.thePlayer.worldObj = this.theWorld;
		System.out.println("Teleported to " + this.theWorld.worldProvider.worldType);
		if(this.thePlayer.isEntityAlive() && var2 < 1) {
			this.thePlayer.setLocationAndAngles(var3, this.thePlayer.posY, var5, this.thePlayer.rotationYaw, this.thePlayer.rotationPitch);
			this.theWorld.updateEntityWithOptionalForce(this.thePlayer, false);
			(new Teleporter()).placeInPortal(this.theWorld, this.thePlayer);
		}

	}

	public void exitToMainMenu(String var1) {
		this.theWorld = null;
		this.changeWorld2((World)null, var1);
	}

	public void changeWorld1(World var1) {
		this.changeWorld2(var1, "");
	}

	public void changeWorld2(World var1, String var2) {
		this.changeWorld(var1, var2, (EntityPlayer)null);
	}

	public void changeWorld(World var1, String var2, EntityPlayer var3) {
		this.statFileWriter.func_27175_b();
		this.statFileWriter.syncStats();
		this.renderViewEntity = null;
		if(this.loadingScreen != null) {
			this.loadingScreen.printText(var2);
			this.loadingScreen.displayLoadingString("");
		}

		this.sndManager.playStreaming((String)null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		if(this.theWorld != null) {
			this.theWorld.saveWorldIndirectly(this.loadingScreen);
		}

		this.theWorld = var1;
		if(var1 != null) {
			if(this.playerController != null) {
				this.playerController.onWorldChange(var1);
			}

			if(!this.isMultiplayerWorld()) {
				if(var3 == null) {
					this.thePlayer = (EntityPlayerSP)var1.func_4085_a(EntityPlayerSP.class);
				}
			} else if(this.thePlayer != null) {
				this.thePlayer.preparePlayerToSpawn();
				if(var1 != null) {
					var1.spawnEntityInWorld(this.thePlayer);
				}
			}

			if(!var1.isRemote) {
				this.preloadWorld(var2);
			}

			if(this.thePlayer == null) {
				this.thePlayer = (EntityPlayerSP)this.playerController.createPlayer(var1);
				this.thePlayer.preparePlayerToSpawn();
				this.playerController.flipPlayer(this.thePlayer);
			}

			this.thePlayer.movementInput = new MovementInputFromOptions(this.gameSettings);
			if(this.renderGlobal != null) {
				this.renderGlobal.changeWorld(var1);
			}

			if(this.effectRenderer != null) {
				this.effectRenderer.clearEffects(var1);
			}

			if(var3 != null) {
				var1.func_6464_c();
			}

			IChunkProvider var4 = var1.getChunkProvider();
			if(var4 instanceof ChunkProviderLoadOrGenerate) {
				ChunkProviderLoadOrGenerate var5 = (ChunkProviderLoadOrGenerate)var4;
				int var6 = MathHelper.floor_float((float)((int)this.thePlayer.posX)) >> 4;
				int var7 = MathHelper.floor_float((float)((int)this.thePlayer.posZ)) >> 4;
				var5.setCurrentChunkOver(var6, var7);
			}

			var1.spawnPlayerWithLoadedChunks(this.thePlayer);
			this.playerController.func_6473_b(this.thePlayer);
			if(var1.isNewWorld) {
				var1.saveWorldIndirectly(this.loadingScreen);
			}

			this.renderViewEntity = this.thePlayer;
		} else {
			this.saveLoader.flushCache();
			this.thePlayer = null;
		}

		System.gc();
		this.systemTime = 0L;
	}

	private void convertMapFormat(String var1, String var2) {
		this.loadingScreen.printText("Converting World to " + this.saveLoader.getFormatName());
		this.loadingScreen.displayLoadingString("This may take a while :)");
		this.saveLoader.convertMapFormat(var1, this.loadingScreen);
		this.startWorld(var1, var2, new WorldSettings(0L, 0, true, false, WorldType.DEFAULT));
	}

	private void preloadWorld(String var1) {
		if(this.loadingScreen != null) {
			this.loadingScreen.printText(var1);
			this.loadingScreen.displayLoadingString(StatCollector.translateToLocal("menu.generatingTerrain"));
		}

		short var2 = 128;
		if(this.playerController.func_35643_e()) {
			var2 = 64;
		}

		int var3 = 0;
		int var4 = var2 * 2 / 16 + 1;
		var4 *= var4;
		IChunkProvider var5 = this.theWorld.getChunkProvider();
		ChunkCoordinates var6 = this.theWorld.getSpawnPoint();
		if(this.thePlayer != null) {
			var6.posX = (int)this.thePlayer.posX;
			var6.posZ = (int)this.thePlayer.posZ;
		}

		if(var5 instanceof ChunkProviderLoadOrGenerate) {
			ChunkProviderLoadOrGenerate var7 = (ChunkProviderLoadOrGenerate)var5;
			var7.setCurrentChunkOver(var6.posX >> 4, var6.posZ >> 4);
		}

		for(int var10 = -var2; var10 <= var2; var10 += 16) {
			for(int var8 = -var2; var8 <= var2; var8 += 16) {
				if(this.loadingScreen != null) {
					this.loadingScreen.setLoadingProgress(var3++ * 100 / var4);
				}

				this.theWorld.getBlockId(var6.posX + var10, 64, var6.posZ + var8);
				if(!this.playerController.func_35643_e()) {
					while(this.theWorld.updatingLighting()) {
					}
				}
			}
		}

		if(!this.playerController.func_35643_e()) {
			if(this.loadingScreen != null) {
				this.loadingScreen.displayLoadingString(StatCollector.translateToLocal("menu.simulating"));
			}

			boolean var9 = true;
			this.theWorld.dropOldChunks();
		}

	}

	public void installResource(String var1, File var2) {
		int var3 = var1.indexOf("/");
		String var4 = var1.substring(0, var3);
		var1 = var1.substring(var3 + 1);
		if(var4.equalsIgnoreCase("sound")) {
			this.sndManager.addSound(var1, var2);
		} else if(var4.equalsIgnoreCase("newsound")) {
			this.sndManager.addSound(var1, var2);
		} else if(var4.equalsIgnoreCase("streaming")) {
			this.sndManager.addStreaming(var1, var2);
		} else if(var4.equalsIgnoreCase("music")) {
			this.sndManager.addMusic(var1, var2);
		} else if(var4.equalsIgnoreCase("newmusic")) {
			this.sndManager.addMusic(var1, var2);
		}

	}

	public String debugInfoRenders() {
		return this.renderGlobal.getDebugInfoRenders();
	}

	public String getEntityDebug() {
		return this.renderGlobal.getDebugInfoEntities();
	}

	public String getWorldProviderName() {
		return this.theWorld.getProviderName();
	}

	public String debugInfoEntities() {
		return "P: " + this.effectRenderer.getStatistics() + ". T: " + this.theWorld.getDebugLoadedEntities();
	}

	public void respawn(boolean var1, int var2, boolean var3) {
		if(!this.theWorld.isRemote && !this.theWorld.worldProvider.canRespawnHere()) {
			this.usePortal(0);
		}

		ChunkCoordinates var4 = null;
		ChunkCoordinates var5 = null;
		boolean var6 = true;
		if(this.thePlayer != null && !var1) {
			var4 = this.thePlayer.getSpawnChunk();
			if(var4 != null) {
				var5 = EntityPlayer.verifyRespawnCoordinates(this.theWorld, var4);
				if(var5 == null) {
					this.thePlayer.addChatMessage("tile.bed.notValid");
				}
			}
		}

		if(var5 == null) {
			var5 = this.theWorld.getSpawnPoint();
			var6 = false;
		}

		IChunkProvider var7 = this.theWorld.getChunkProvider();
		if(var7 instanceof ChunkProviderLoadOrGenerate) {
			ChunkProviderLoadOrGenerate var8 = (ChunkProviderLoadOrGenerate)var7;
			var8.setCurrentChunkOver(var5.posX >> 4, var5.posZ >> 4);
		}

		this.theWorld.setSpawnLocation();
		this.theWorld.updateEntityList();
		int var10 = 0;
		if(this.thePlayer != null) {
			var10 = this.thePlayer.entityId;
			this.theWorld.setEntityDead(this.thePlayer);
		}

		EntityPlayerSP var9 = this.thePlayer;
		this.renderViewEntity = null;
		this.thePlayer = (EntityPlayerSP)this.playerController.createPlayer(this.theWorld);
		if(var3) {
			this.thePlayer.copyPlayer(var9);
		}

		this.thePlayer.dimension = var2;
		this.renderViewEntity = this.thePlayer;
		this.thePlayer.preparePlayerToSpawn();
		if(var6) {
			this.thePlayer.setSpawnChunk(var4);
			this.thePlayer.setLocationAndAngles((double)((float)var5.posX + 0.5F), (double)((float)var5.posY + 0.1F), (double)((float)var5.posZ + 0.5F), 0.0F, 0.0F);
		}

		this.playerController.flipPlayer(this.thePlayer);
		this.theWorld.spawnPlayerWithLoadedChunks(this.thePlayer);
		this.thePlayer.movementInput = new MovementInputFromOptions(this.gameSettings);
		this.thePlayer.entityId = var10;
		this.thePlayer.func_6420_o();
		this.playerController.func_6473_b(this.thePlayer);
		this.preloadWorld(StatCollector.translateToLocal("menu.respawning"));
		if(this.currentScreen instanceof GuiGameOver) {
			this.displayGuiScreen((GuiScreen)null);
		}

	}

	public static void startMainThread1(String var0, String var1) throws LWJGLException {
		startMainThread(var0, var1, (String)null);
	}

	public static void startMainThread(String var0, String var1, String var2) throws LWJGLException {
		boolean var3 = false;
		Frame var5 = new Frame("Minecraft");
		Canvas var6 = new Canvas();
		var5.setLayout(new BorderLayout());
		var5.add(var6, "Center");
		var6.setPreferredSize(new Dimension(854, 480));
		var5.pack();
		var5.setLocationRelativeTo((Component)null);
		MinecraftImpl var7 = new MinecraftImpl(var5, var6, (MinecraftApplet)null, 854, 480, var3, var5);
		Thread var8 = new Thread(var7, "Minecraft main thread");
		var8.setPriority(10);
		var7.minecraftUri = "www.minecraft.net";
		if(var0 != null && var1 != null) {
			var7.session = new Session(var0, var1);
		} else {
			var7.session = new Session("Player" + System.currentTimeMillis() % 1000L, "");
		}

		if(var2 != null) {
			String[] var9 = var2.split(":");
			var7.setServer(var9[0], Integer.parseInt(var9[1]));
		}

		var5.setVisible(true);
		var5.addWindowListener(new GameWindowListener(var7, var8));
		var8.start();
	}

	public NetClientHandler getSendQueue() {
		return this.thePlayer instanceof EntityClientPlayerMP ? ((EntityClientPlayerMP)this.thePlayer).sendQueue : null;
	}

	public static void main(String[] var0) throws LWJGLException {
		String var1 = null;
		String var2 = null;
		var1 = "Player" + System.currentTimeMillis() % 1000L;
		if(var0.length > 0) {
			var1 = var0[0];
		}

		var2 = "-";
		if(var0.length > 1) {
			var2 = var0[1];
		}

		startMainThread1(var1, var2);
	}

	public static boolean isGuiEnabled() {
		return theMinecraft == null || !theMinecraft.gameSettings.hideGUI;
	}

	public static boolean isFancyGraphicsEnabled() {
		return theMinecraft != null && theMinecraft.gameSettings.fancyGraphics;
	}

	public static boolean isAmbientOcclusionEnabled() {
		return theMinecraft != null && theMinecraft.gameSettings.ambientOcclusion;
	}

	public static boolean isDebugInfoEnabled() {
		return theMinecraft != null && theMinecraft.gameSettings.showDebugInfo;
	}

	public boolean lineIsCommand(String var1) {
		if(var1.startsWith("/")) {
		}

		return false;
	}

	private void clickMiddleMouseButton() {
		if(this.objectMouseOver != null) {
			boolean var1 = this.thePlayer.capabilities.isCreativeMode;
			int var2 = this.theWorld.getBlockId(this.objectMouseOver.blockX, this.objectMouseOver.blockY, this.objectMouseOver.blockZ);
			if(!var1) {
				if(var2 == Block.grass.blockID) {
					var2 = Block.dirt.blockID;
				}

				if(var2 == Block.stairDouble.blockID) {
					var2 = Block.stairSingle.blockID;
				}

				if(var2 == Block.bedrock.blockID) {
					var2 = Block.stone.blockID;
				}
			}

			int var3 = 0;
			boolean var4 = false;
			if(Item.itemsList[var2] != null && Item.itemsList[var2].getHasSubtypes()) {
				var3 = this.theWorld.getBlockMetadata(this.objectMouseOver.blockX, this.objectMouseOver.blockY, this.objectMouseOver.blockZ);
				var4 = true;
			}

			if(Item.itemsList[var2] != null && Item.itemsList[var2] instanceof ItemBlock) {
				Block var5 = Block.blocksList[var2];
				int var6 = var5.idDropped(var3, this.thePlayer.worldObj.rand, 0);
				if(var6 > 0) {
					var2 = var6;
				}
			}

			this.thePlayer.inventory.setCurrentItem(var2, var3, var4, var1);
			if(var1) {
				int var7 = this.thePlayer.inventorySlots.inventorySlots.size() - 9 + this.thePlayer.inventory.currentItem;
				this.playerController.sendSlotPacket(this.thePlayer.inventory.getStackInSlot(this.thePlayer.inventory.currentItem), var7);
			}
		}

	}

	public static String func_52003_C() {
		return "1.2.5";
	}

	public static void func_52004_D() {
		PlayerUsageSnooper var0 = new PlayerUsageSnooper("client");
		var0.func_52022_a("version", func_52003_C());
		var0.func_52022_a("os_name", System.getProperty("os.name"));
		var0.func_52022_a("os_version", System.getProperty("os.version"));
		var0.func_52022_a("os_architecture", System.getProperty("os.arch"));
		var0.func_52022_a("memory_total", Long.valueOf(Runtime.getRuntime().totalMemory()));
		var0.func_52022_a("memory_max", Long.valueOf(Runtime.getRuntime().maxMemory()));
		var0.func_52022_a("java_version", System.getProperty("java.version"));
		var0.func_52022_a("opengl_version", GL11.glGetString(GL11.GL_VERSION));
		var0.func_52022_a("opengl_vendor", GL11.glGetString(GL11.GL_VENDOR));
		var0.func_52021_a();
	}
}
