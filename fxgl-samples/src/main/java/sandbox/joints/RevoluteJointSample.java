/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package sandbox.joints;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.physics.box2d.dynamics.joints.RevoluteJoint;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * TODO: a unified physics simulation sample to extend from, which provides
 * ability to shoot a physics projectile and spawn various physics objects.
 *
 * Shows how to use RevoluteJoints with PhysicsComponent.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class RevoluteJointSample extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("LMB") {
            private double x;
            private double y;

            @Override
            protected void onActionBegin() {
                x = getInput().getMouseXWorld();
                y = getInput().getMouseYWorld();
            }

            @Override
            protected void onActionEnd() {
                var endx = getInput().getMouseXWorld();
                var endy = getInput().getMouseYWorld();

                spawnBullet(x, y, endx - x, endy - y);
            }
        }, MouseButton.PRIMARY);

        onKeyDown(KeyCode.F, () -> {
            Entity box = createPhysicsEntity();

            box.getBoundingBoxComponent()
                    .addHitBox(new HitBox("Left", BoundingShape.box(40, 40)));
            box.getBoundingBoxComponent()
                    .addHitBox(new HitBox("Right", new Point2D(40, 0), BoundingShape.box(40, 40)));

            box.getViewComponent().addChild(texture("brick.png", 40, 40).superTexture(texture("brick.png", 40, 40), HorizontalDirection.RIGHT));
            box.setRotationOrigin(new Point2D(40, 20));

            getGameWorld().addEntity(box);
        });

        onBtnDown(MouseButton.SECONDARY, () -> {
            Entity ball = createPhysicsEntity();

            ball.getBoundingBoxComponent()
                    .addHitBox(new HitBox("Test", BoundingShape.circle(20)));
            ball.getViewComponent().addChild(texture("ball.png", 40, 40));
            ball.setRotationOrigin(new Point2D(20, 20));

            getGameWorld().addEntity(ball);
        });
    }

    private RevoluteJoint joint;

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.LIGHTGRAY);

        entityBuilder()
                .buildScreenBoundsAndAttach(50);

        // platform

        entityBuilder()
                .at(400, 400)
                .viewWithBBox(new Rectangle(500, 20, Color.BROWN))
                .with(new PhysicsComponent())
                .buildAndAttach();

        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().density(1.1f));
        physics.setBodyType(BodyType.DYNAMIC);

        Entity block = entityBuilder()
                .at(600, 100)
                .viewWithBBox(new Rectangle(80, 50))
                .with(physics)
                .buildAndAttach();

        PhysicsComponent physics2 = new PhysicsComponent();
        physics2.setBodyType(BodyType.DYNAMIC);

        FixtureDef fd = new FixtureDef();
        fd.setDensity(1.0f);
        physics2.setFixtureDef(fd);

        Entity ball1 = entityBuilder()
                .at(600, 360)
                .bbox(new HitBox("main", BoundingShape.circle(15)))
                .view(texture("ball.png", 30, 30))
                .with(physics2)
                .buildAndAttach();

        PhysicsComponent physics3 = new PhysicsComponent();
        physics3.setBodyType(BodyType.DYNAMIC);
        physics3.setFixtureDef(fd);

        Entity ball2 = entityBuilder()
                .at(700, 360)
                .bbox(new HitBox("main", BoundingShape.circle(15)))
                .view(texture("ball.png", 30, 30))
                .with(physics3)
                .buildAndAttach();

        physics2.getBody().setAngularDamping(1f);
        physics3.getBody().setAngularDamping(1f);

        getPhysicsWorld().addRevoluteJoint(block, ball1, new Point2D(80, 50), new Point2D(15, 15));
        getPhysicsWorld().addRevoluteJoint(block, ball2, new Point2D(0, 50), new Point2D(15, 15));
    }

    private void spawnBullet(double x, double y, double vx, double vy) {
        var physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().density(25.5f).restitution(0.5f));
        physics.setBodyType(BodyType.DYNAMIC);

        physics.setOnPhysicsInitialized(() -> {
            physics.setLinearVelocity(vx * 10, vy * 10);
        });

        entityBuilder()
                .at(x, y)
                .bbox(new HitBox(BoundingShape.circle(450 / 15.0 / 2.0)))
                .view(texture("ball.png", 450 / 15.0, 449 / 15.0))
                .with(physics)
                .with(new ExpireCleanComponent(Duration.seconds(5)).animateOpacity())
                .buildAndAttach();
    }

    private Entity createPhysicsEntity() {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().density(0.1f).restitution(0.3f));

        return entityBuilder()
                .at(getInput().getMousePositionWorld())
                .with(physics)
                .build();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
