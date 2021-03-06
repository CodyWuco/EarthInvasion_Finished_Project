package com.gamecodeschool.earthinvasion;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;

class GameEngine extends SurfaceView
        implements Runnable,
        GameStarter,
        GameEngineBroadcaster {

    private Thread mThread = null;
    private long mFPS;

    private ArrayList<InputObserver> inputObservers = new ArrayList();
    UIController mUIController;

    private GameState mGameState;
    private SoundEngine mSoundEngine;
    HUD mHUD;
    Renderer mRenderer;
    ParticleSystem mParticleSystem;
    PhysicsEngine mPhysicsEngine;
    Level mLevel;
    LaserOrbPool mLaserOrbs;


    public GameEngine(Context context, Point size) {
        super(context);

        mUIController = new UIController(this);
        mGameState = new GameState(this, context);
        mSoundEngine = new SoundEngine(context);
        mHUD = new HUD(size);
        mRenderer = new Renderer(this);
        mPhysicsEngine = new PhysicsEngine();

        mParticleSystem = new ParticleSystem();
        mParticleSystem.init(1000);

        mLevel = new Level(context,
                new PointF(size.x, size.y), this);
        mLaserOrbs = new LaserOrbPool(mLevel, mSoundEngine);
    }

    // For the game engine broadcaster interface
    public void addObserver(InputObserver o) {
        inputObservers.add(o);
    }

    // spawns should be handled by the level
    public void deSpawnReSpawn() { mLevel.deSpawnReSpawn(); }

    @Override
    public void run() {
        while (mGameState.getThreadRunning()) {
            long frameStartTime = System.currentTimeMillis();
            ArrayList<GameObject> objects = mLevel.getGameObjects();
            ArrayList<GameObject> enemyObjects = mLevel.getEnemyGameObjects();

            if (!mGameState.getPaused()) {
                mLevel.update(mFPS);
                if(mPhysicsEngine.update(mFPS, objects, enemyObjects,
                        mGameState, mSoundEngine, mParticleSystem)){
                    // Player hit
                    //deSpawnReSpawn();
                }
            }

            // Draw all the game objects here
            // in a new way
            mRenderer.draw(objects, mGameState, mHUD, mParticleSystem);

            // Measure the frames per second in the usual way
            long timeThisFrame = System.currentTimeMillis()
                    - frameStartTime;
            if (timeThisFrame >= 1) {
                final int MILLIS_IN_SECOND = 1000;
                mFPS = MILLIS_IN_SECOND / timeThisFrame;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Handle the player's input here
        // But in a new way
        for (InputObserver o : inputObservers) {
            o.handleInput(motionEvent, mGameState, mHUD.getControls());
        }



        return true;
    }

    public void stopThread() {
        // New code here soon
        mGameState.stopEverything();

        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.e("Exception","stopThread()" + e.getMessage());
        }
    }

    public void startThread() {
        // New code here soon
        mGameState.startThread();

        mThread = new Thread(this);
        mThread.start();
    }


    public void shootSmallLaserOrb(GameObject shooter, GameObject target){
        mLaserOrbs.shootSmallLaserOrb(shooter, target);
    }

    public void shootLargeLaserOrb(GameObject shooter, GameObject target){
        mLaserOrbs.shootLargeLaserOrb(shooter, target);
    }
}