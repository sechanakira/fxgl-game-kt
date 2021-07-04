package com.github.sechanakira.component

import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.component.Component
import javafx.geometry.Point2D

class CloudComponent : Component() {

    companion object {
        val direction = Point2D(FXGLMath.random(-1.0, 1.0), FXGLMath.random(-1.0, 1.0))
    }

    override fun onUpdate(tpf: Double) {
        entity.translate(direction.multiply(3.0))
        checkForBounds()
    }

    private fun checkForBounds() {
        if (entity.x < 0) {
            remove()
        }
        if (entity.x >= FXGL.getAppWidth()) {
            remove()
        }
        if (entity.y < 0) {
            remove()
        }
        if (entity.y >= FXGL.getAppHeight()) {
            remove()
        }
    }

    private fun remove() {
        entity.removeFromWorld()
    }
}

class PlayerComponent : Component() {
    companion object {
        const val ROTATION_CHANGE = 0.01
        var direction = Point2D(1.0, 1.0)
    }

    override fun onUpdate(tpf: Double) {
        entity.translate(direction.multiply(1.0))
        checkForBounds()
    }

    private fun checkForBounds() {
        if (entity.x < 0) {
            die()
        }
        if (entity.x >= FXGL.getAppWidth()) {
            die()
        }
        if (entity.y < 0) {
            die()
        }
        if (entity.y >= FXGL.getAppHeight()) {
            die()
        }
    }

    fun shoot() {
        FXGL.spawn(
            "bullet", SpawnData(
                getEntity().position.x + 20,
                getEntity().position.y - 5
            )
                .put("direction", direction)
        )
    }

    fun die() {
        FXGL.inc("lives", -1)
        if (FXGL.geti("lives") <= 0) {
            FXGL.getDialogService().showMessageBox(
                "Game Over"
            ) { FXGL.getGameController().startNewGame() }
            return
        }
        entity.setPosition(0.0, 0.0)
        direction = Point2D(1.0, 1.0)
        right()
    }

    fun up() {
        if (direction.y > -1) {
            direction = Point2D(direction.x, direction.y - ROTATION_CHANGE)
        }
    }

    fun down() {
        if (direction.y < 1) {
            direction = Point2D(direction.x, direction.y + ROTATION_CHANGE)
        }
    }

    fun left() {
        if (direction.x > -1) {
            direction = Point2D(direction.x - ROTATION_CHANGE, direction.y)
        }
    }

    fun right() {
        if (direction.x < 1) {
            direction = Point2D(direction.x + ROTATION_CHANGE, direction.y)
        }
    }
}
