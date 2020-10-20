package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.UBJsonReader;
import com.badlogic.gdx.utils.async.AsyncTask;

import javax.management.Notification;

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;

public class app extends ApplicationAdapter implements GestureDetector.GestureListener {
	public static float number = 0f;
	public static int pos = 0;
	public static float[] pos2 = {0,0,0,0,0,0};
	public static String[] cons = {"0" ,"0", "0", "0"};
	private Array<BlendingAttribute> blendArray = new Array<BlendingAttribute>();
	private Material mat;
	private BlendingAttribute blend;
	private PerspectiveCamera camera;
	private ModelBatch modelBatch;
	private ModelBuilder modelBuilder;
	private Model model;
	private Model sphere;
	private ModelInstance modelInstance;
	private Array<ModelInstance> modelSphere = new Array<ModelInstance>();
	private Environment environment;
	private AnimationController controller;
	private PointLight pointLight;
	private GestureDetector gestureDetector;
	private int X1, Y1, X2,Y2;
	private static Vector3 X;
	private static Vector3 Y;
	private BitmapFont font;
	private SpriteBatch batch;


	public interface androidIntent {
		void start();
		void modeSwitch();
	}
	Stage stage;
	TextButton button;
	TextButton.TextButtonStyle textButtonStyle;

	Skin skin;
	TextureAtlas buttonAtlas;
	private void buttonCreation(){
		stage = new Stage();
        Gdx.input.setInputProcessor(stage);
		/*
		*
		* Table creation
		*
		* */
        Table table = new Table();
        table.setFillParent(true);


		font = new BitmapFont();
		skin = new Skin();
		buttonAtlas = new TextureAtlas(Gdx.files.internal("TextureAtlas.pack"));
		skin.addRegions(buttonAtlas);
		textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.font = font;
		textButtonStyle.up = skin.getDrawable("alarm");
		textButtonStyle.down = skin.getDrawable("alarm");
		textButtonStyle.checked = skin.getDrawable("noalarm");
        button = new TextButton("", textButtonStyle);
        table.add(button).expand().top().left();
		//stage.addActor(button);
        stage.addActor(table);
	}


    private androidIntent AndroidIntent;
	public void setCallback(androidIntent intent){
		AndroidIntent = intent;
	}
	private void callStart(){ AndroidIntent.start();
	}
	@Override
	public void create () {
		if(AndroidIntent != null)
			callStart();
		/*          Text rendering part         */
		font = new BitmapFont();
		font.getData().setScale(2);
		batch = new SpriteBatch();

		X = new Vector3(0f,0f,0f);
		Y = new Vector3(0f,0f,0f);
		camera = new PerspectiveCamera(75,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		camera.position.set(700f, 800f, 600f);
		camera.lookAt(0f,100f,0f);
		camera.near = 100f;
		camera.far = 2000.0f;

		blend = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		mat = new Material(ColorAttribute.createDiffuse(Color.BLUE));
		blend.opacity = 0f;
		mat.set(blend);

		modelBatch = new ModelBatch();
		modelBuilder = new ModelBuilder();
		sphere = modelBuilder.createSphere(600f, 900f, 600f, 20, 20,
				mat, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

		UBJsonReader jsonReader = new UBJsonReader();

		G3dModelLoader modelLoader = new G3dModelLoader(jsonReader);
		model = modelLoader.loadModel(Gdx.files.getFileHandle("finalSborka.g3db", Files.FileType.Internal));
		modelInstance = new ModelInstance(model);

		modelSphere.add(new ModelInstance(sphere, -700f,25f,0f));
		modelSphere.add(new ModelInstance(sphere, -250f,25f,0f));
		modelSphere.add(new ModelInstance(sphere, -100f,25f,500f));
		modelSphere.add(new ModelInstance(sphere, 300f,25f,-200f));


		blendArray.setSize(6);
		for(int i = 0; i< blendArray.size; i++){
			blendArray.set(i, new BlendingAttribute());
			blendArray.get(i).opacity = 0;
		}

		environment = new Environment();
		environment.add(new DirectionalLight().set(0.4f, 0.4f, 0.4f, 100f, -60f, 100f));

		gestureDetector = new GestureDetector(this);
		Gdx.input.setInputProcessor(gestureDetector);

	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		model.dispose();

        skin.dispose();
        buttonAtlas.dispose();
        font.dispose();
        stage.dispose();
	}
	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0.3f, 0.5f, 0.7f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		camera.update();
		modelBatch.begin(camera);
		batch.begin();
		font.draw(batch, "Zona 1: " + cons[0], 20f, Gdx.graphics.getHeight());
		font.draw(batch, "Zona 2: " + cons[1], 20f, Gdx.graphics.getHeight() - 30);
		font.draw(batch, "Zona 3: " + cons[2], 20f, Gdx.graphics.getHeight() - 60);
		font.draw(batch, "Zona 4: " + cons[3], 20f, Gdx.graphics.getHeight() - 90);
		batch.end();
		modelBatch.render(modelInstance, environment);
		//blend.opacity = number;
		for(int i = 0; i < modelSphere.size; i++){
			//if(i == pos) blendArray.get(i).opacity = number;
			blendArray.get(i).opacity = pos2[i];
			modelSphere.get(i).materials.get(0).set(blendArray.get(i));
			modelBatch.render(modelSphere.get(i));
		}


		modelBatch.end();
		Gdx.graphics.setContinuousRendering(false);

	}


	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		Gdx.app.log("longPress", "PRESSED");
		AndroidIntent.modeSwitch();
		return true;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {

		Y.set(0, deltaX, 0f);
		camera.rotateAround(X, Y, 1f);
		camera.update();
		Gdx.graphics.requestRendering();
		return true;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return false;
	}

	@Override
	public void pinchStop() {

	}




}
