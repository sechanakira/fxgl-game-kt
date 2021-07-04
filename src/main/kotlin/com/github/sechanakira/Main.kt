package com.github.sechanakira

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.components.AutoRotationComponent
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent
import com.almasb.fxgl.dsl.components.ProjectileComponent
import com.almasb.fxgl.dsl.getGameScene
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.entity.components.IrremovableComponent
import com.github.sechanakira.component.CloudComponent
import com.github.sechanakira.component.PlayerComponent
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import java.awt.Toolkit

fun main(args: Array<String>) {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val screenWidth = screenSize.getWidth().toInt()
    val screenHeight = screenSize.getHeight().toInt()
    val app = GameApp(screenWidth, screenHeight)
    app.startGame(args)
}

class GameApp(private val screenWidth: Int, private val screenHeight: Int) : GameApplication() {
    companion object {
        val gameFactory = GameFactory()
    }

    private lateinit var player: Entity

    /**
     * General game settings.
     *
     * @param settings The settings of the game which can be further extended here.
     */
    override fun initSettings(settings: GameSettings) {
        settings.height = screenHeight
        settings.width = screenWidth
        settings.isFullScreenAllowed = true
        settings.isFullScreenFromStart = true
        settings.title = "Oracle Java Magazine - FXGL"
    }

    fun startGame(args: Array<String>) {
        launch(this::class.java, args)
    }

    /**
     * Input configuration, here you configure all the input events like key presses, mouse clicks, etc.
     */
    override fun initInput() {
        FXGL.onKey(KeyCode.LEFT, "left") { player.getComponent(PlayerComponent::class.java).left() }
        FXGL.onKey(KeyCode.RIGHT, "right") { player.getComponent(PlayerComponent::class.java).right() }
        FXGL.onKey(KeyCode.UP, "up") { player.getComponent(PlayerComponent::class.java).up() }
        FXGL.onKey(KeyCode.DOWN, "down") { player.getComponent(PlayerComponent::class.java).down() }
        FXGL.onKeyDown(KeyCode.SPACE, "Bullet") { player.getComponent(PlayerComponent::class.java).shoot() }
    }

    /**
     * General game variables. Used to hold the points and lives.
     *
     * @param vars The variables of the game which can be further extended here.
     */
    override fun initGameVars(vars: MutableMap<String?, Any?>) {
        vars["score"] = 0
        vars["lives"] = 5
    }

    /**
     * Initialization of the game by providing the [EntityFactory].
     */
    override fun initGame() {
        FXGL.getGameWorld().addEntityFactory(gameFactory)
        FXGL.spawn(
            "background", SpawnData(0.0, 0.0).put("width", FXGL.getAppWidth())
                .put("height", FXGL.getAppHeight())
        )
        val circleRadius = 80
        FXGL.spawn(
            "center", SpawnData(
                (FXGL.getAppWidth() / 2 - circleRadius / 2).toDouble(),
                (FXGL.getAppHeight() / 2 - circleRadius / 2).toDouble()
            )
                .put("x", circleRadius / 2)
                .put("y", circleRadius / 2)
                .put("radius", circleRadius)
        )

        // Add the player
        player = FXGL.spawn("duke", 0.0, 0.0)
    }

    /**
     * Initialization of the collision handlers.
     */
    override fun initPhysics() {
        FXGL.onCollisionBegin(
            GameFactory.EntityType.DUKE, GameFactory.EntityType.CENTER
        ) { duke: Entity?, center: Entity? ->
            player.getComponent(
                PlayerComponent::class.java
            ).die()
        }
        FXGL.onCollisionBegin(
            GameFactory.EntityType.DUKE, GameFactory.EntityType.CLOUD
        ) { enemy: Entity?, cloud: Entity? ->
            player.getComponent(
                PlayerComponent::class.java
            ).die()
        }
        FXGL.onCollisionBegin(
            GameFactory.EntityType.BULLET,
            GameFactory.EntityType.CLOUD
        ) { bullet: Entity, cloud: Entity ->
            FXGL.inc("score", 1)
            bullet.removeFromWorld()
            cloud.removeFromWorld()
        }
    }

    /**
     * Configuration of the user interface.
     */
    override fun initUI() {
        val scoreLabel = FXGL.getUIFactoryService().newText("Score", Color.BLACK, 22.0)
        val scoreValue = FXGL.getUIFactoryService().newText("", Color.BLACK, 22.0)
        val livesLabel = FXGL.getUIFactoryService().newText("Lives", Color.BLACK, 22.0)
        val livesValue = FXGL.getUIFactoryService().newText("", Color.BLACK, 22.0)
        scoreLabel.translateX = 20.0
        scoreLabel.translateY = 20.0
        scoreValue.translateX = 90.0
        scoreValue.translateY = 20.0
        livesLabel.translateX = (FXGL.getAppWidth() - 150).toDouble()
        livesLabel.translateY = 20.0
        livesValue.translateX = (FXGL.getAppWidth() - 80).toDouble()
        livesValue.translateY = 20.0
        scoreValue.textProperty().bind(FXGL.getWorldProperties().intProperty("score").asString())
        livesValue.textProperty().bind(FXGL.getWorldProperties().intProperty("lives").asString())
        getGameScene().addUINodes(scoreLabel, scoreValue, livesLabel, livesValue)
    }

    /**
     * Gets called every frame _only_ in Play state.
     */
    override fun onUpdate(tpf: Double) {
        if (FXGL.getGameWorld().getEntitiesByType(GameFactory.EntityType.CLOUD).size < 10) {
            FXGL.spawn("cloud", (FXGL.getAppWidth() / 2).toDouble(), (FXGL.getAppHeight() / 2).toDouble())
        }
    }
}

/**
 * The factory which defines how each entity looks like
 */
class GameFactory : EntityFactory {
    /**
     * Types of objects we are going to use in our game.
     */
    enum class EntityType {
        BACKGROUND, CENTER, DUKE, CLOUD, BULLET
    }

    @Spawns("background")
    fun spawnBackground(data: SpawnData): Entity? {
        return FXGL.entityBuilder(data)
            .type(EntityType.BACKGROUND)
            .view(Rectangle(data.get<Int>("width").toDouble(), data.get<Int>("height").toDouble(), Color.YELLOWGREEN))
            .with(IrremovableComponent())
            .zIndex(-100)
            .build()
    }

    @Spawns("center")
    fun spawnCenter(data: SpawnData): Entity? {
        return FXGL.entityBuilder(data)
            .type(EntityType.CENTER)
            .collidable()
            .viewWithBBox(
                Circle(
                    data.get<Int>("x").toDouble(), data.get<Int>("y").toDouble(),
                    data.get<Int>("radius").toDouble(), Color.DARKRED
                )
            )
            .with(IrremovableComponent())
            .zIndex(-99)
            .build()
    }

    @Spawns("duke")
    fun newDuke(data: SpawnData?): Entity? {
        return FXGL.entityBuilder(data!!)
            .type(EntityType.DUKE)
            .viewWithBBox(FXGL.texture("duke.png", 50.0, 50.0))
            .collidable()
            .with(AutoRotationComponent().withSmoothing())
            .with(PlayerComponent())
            .build()
    }

    @Spawns("cloud")
    fun newCloud(data: SpawnData?): Entity? {
        return FXGL.entityBuilder(data!!)
            .type(EntityType.CLOUD)
            .viewWithBBox(FXGL.texture("cloud-network.png", 50.0, 50.0))
            .with(AutoRotationComponent().withSmoothing())
            .with(CloudComponent())
            .collidable()
            .build()
    }

    @Spawns("bullet")
    fun newBullet(data: SpawnData): Entity? {
        return FXGL.entityBuilder(data)
            .type(EntityType.BULLET)
            .viewWithBBox(FXGL.texture("sprite_bullet.png", 22.0, 11.0))
            .collidable()
            .with(ProjectileComponent(data.get("direction"), 350.0), OffscreenCleanComponent())
            .build()
    }
}