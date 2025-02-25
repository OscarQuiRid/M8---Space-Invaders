package com.example.spaceinvader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.Random;

class Booster {
    int x, y;
    Bitmap bitmap;
    private int speed;

    Booster(int x, int y, Bitmap bitmap) {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
        this.speed = 5 + new Random().nextInt(5);
    }

    void move() {
        x += speed;
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    Rect getRect() {
        return new Rect(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
    }
}