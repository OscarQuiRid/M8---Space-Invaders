package com.example.spaceinvader;

import android.graphics.Bitmap;
import android.graphics.Canvas;

class Enemy {
    int x, y;
    private int enemySpeed = 5;
    Bitmap bitmap;

    Enemy(int x, int y, Bitmap bitmap) {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
    }

    void move() {
        x += enemySpeed;

        if (y < 0) {
            y = 0;
            enemySpeed = Math.abs(enemySpeed);
        }

        if (y + bitmap.getHeight() > GameView.SCREEN_HEIGHT) {
            y = GameView.SCREEN_HEIGHT - bitmap.getHeight();
            enemySpeed = -enemySpeed;
        }
    }

    void increaseSpeed(int increment) {
        if (enemySpeed > 0) {
            enemySpeed += increment;
        } else {
            enemySpeed -= increment;
        }
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    android.graphics.Rect getRect() {
        return new android.graphics.Rect(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
    }

    public int getEnemySpeed() {
        return enemySpeed;
    }

    public void setEnemySpeed(int enemySpeed) {
        this.enemySpeed = enemySpeed;
    }
}