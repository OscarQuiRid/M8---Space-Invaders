package com.example.spaceinvader;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends View {

    private String playerName;
    private Bitmap playerBitmap, enemyBitmap, bulletPlayerBitmap, bulletEnemyBitmap, boosterBitmap, backgroundBitmap;
    private int playerY = 500, playerX = 100;
    private int playerAmmo = 50, enemyShotInterval = 3000;
    private int level = 1;
    private long startTime = System.currentTimeMillis();

    private ArrayList<Bullet> bulletsPlayer = new ArrayList<>();
    private ArrayList<Bullet> bulletsEnemy = new ArrayList<>();
    private ArrayList<Booster> boosters = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();

    private long lastBoosterTime = System.currentTimeMillis();
    private long lastEnemyShotTime = System.currentTimeMillis();
    private long lastEnemySpawnTime = System.currentTimeMillis();
    private boolean gameOver = false;
    private String winner = "";

    private Paint textPaint;


    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public GameView(Context context, String playerName) {
        super(context);
        this.playerName = playerName;
        SCREEN_WIDTH = getResources().getDisplayMetrics().widthPixels;
        SCREEN_HEIGHT = getResources().getDisplayMetrics().heightPixels;

        playerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        enemyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.enemigo);
        bulletPlayerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ataqueplayer);
        bulletEnemyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ataqueenemigo);
        boosterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.booster);
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fondo);

        playerX = SCREEN_WIDTH - playerBitmap.getWidth() - 50;

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        new Thread(() -> {
            while (!gameOver) {
                updateGame();
                postInvalidate();
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(playerBitmap, playerX, playerY, null);

        for (Enemy enemy : enemies) enemy.draw(canvas);
        for (Bullet bullet : bulletsPlayer) bullet.draw(canvas);
        for (Bullet bullet : bulletsEnemy) bullet.draw(canvas);
        for (Booster booster : boosters) booster.draw(canvas);

        canvas.drawText("Munición: " + playerAmmo, SCREEN_WIDTH - 300, 100, textPaint);
        int secondsLeft = 30 - (int) ((System.currentTimeMillis() - startTime) / 1000);

        if (level < 10 && secondsLeft <= 0) {
            levelUp();
            startTime = System.currentTimeMillis();
        }

        String timeText = (level == 10) ? "00:00" : String.format("00:%02d", Math.max(0, secondsLeft));
        canvas.drawText("Jugador: " + playerName, getWidth() / 2 - 50, 100, textPaint);
        canvas.drawText(timeText, getWidth() / 2 - 50, 150, textPaint);
        canvas.drawText("Nivel: " + level, 50, 100, textPaint);

        if (gameOver) {
            Paint gameOverPaint = new Paint();
            gameOverPaint.setColor(Color.RED);
            gameOverPaint.setTextSize(100);
            gameOverPaint.setTypeface(Typeface.DEFAULT_BOLD);
            String message = winner.equals("Player") ? "YOU WIN" : "Game Over";
            canvas.drawText(message, getWidth() / 2 - 200, getHeight() / 2, gameOverPaint);
        }
    }

    private void updateGame() {
        if (gameOver) return;

        bulletsPlayer.removeIf(bullet -> bullet.move(-20, SCREEN_WIDTH));
        bulletsEnemy.removeIf(bullet -> bullet.move(10, SCREEN_WIDTH));


        for (Enemy enemy : enemies) {
            enemy.move();
        }

        for (Booster booster : boosters) {
            booster.move();
        }

        for (int i = 0; i < enemies.size(); i++) {
            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy enemy1 = enemies.get(i);
                Enemy enemy2 = enemies.get(j);

                if (pixelPerfectCollision(enemy1.bitmap, enemy1.x, enemy1.y, enemy2.bitmap, enemy2.x, enemy2.y)) {
                    enemy1.setEnemySpeed(-enemy1.getEnemySpeed());
                    enemy2.setEnemySpeed(-enemy2.getEnemySpeed());
                }
            }
        }

        // Colisiones entre enemigos y el jugador
        for (Enemy enemy : enemies) {
            if (pixelPerfectCollision(enemy.bitmap, enemy.x, enemy.y, playerBitmap, playerX, playerY)) {
                gameOver = true;
                winner = "Enemigo";
                return;
            }
        }

        // Disparo de los enemigos solo si el nivel es mayor que 1
        if (level > 1 && System.currentTimeMillis() - lastEnemyShotTime > enemyShotInterval) {
            for (Enemy enemy : enemies) {
                bulletsEnemy.add(new Bullet(enemy.x, enemy.y, bulletEnemyBitmap));
            }
            lastEnemyShotTime = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - lastBoosterTime > 5000) {
            boosters.add(new Booster(0, new Random().nextInt(SCREEN_HEIGHT - 100), boosterBitmap));
            lastBoosterTime = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - lastEnemySpawnTime > 5000) {
            if (enemies.size() < (level + 1)) {
                spawnEnemies();
            }
            lastEnemySpawnTime = System.currentTimeMillis();
        }

        for (Iterator<Booster> iter = boosters.iterator(); iter.hasNext(); ) {
            Booster booster = iter.next();
            booster.move();

            if (booster.x + booster.bitmap.getWidth() < 0) {
                iter.remove();
            }
        }

        detectCollisions();
    }

    private void spawnEnemies() {
        if (enemies.size() >= level + 1) return;
        Random random = new Random();
        int enemyX = 0;
        int enemyY = random.nextInt(SCREEN_HEIGHT - enemyBitmap.getHeight());
        enemies.add(new Enemy(enemyX, enemyY, enemyBitmap));
    }

    private void levelUp() {
        if (level < 10) {
            level++;

            for (Enemy enemy : enemies) {
                enemy.increaseSpeed(3);
            }

            if (enemyShotInterval > 1000) enemyShotInterval -= 50;
            spawnEnemies();
        }
    }

    private boolean pixelPerfectCollision(Bitmap bitmap1, int x1, int y1, Bitmap bitmap2, int x2, int y2) {
        int left = Math.max(x1, x2);
        int right = Math.min(x1 + bitmap1.getWidth(), x2 + bitmap2.getWidth());
        int top = Math.max(y1, y2);
        int bottom = Math.min(y1 + bitmap1.getHeight(), y2 + bitmap2.getHeight());

        if (left >= right || top >= bottom) return false; // No hay intersección

        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                int pixel1 = bitmap1.getPixel(x - x1, y - y1);
                int pixel2 = bitmap2.getPixel(x - x2, y - y2);

                if (((pixel1 >> 24) & 255) > 0 && ((pixel2 >> 24) & 255) > 0) {
                    return true; // Ambos píxeles no son transparentes
                }
            }
        }
        return false;
    }

    private void detectCollisions() {
        ArrayList<Bullet> bulletsToRemovePlayer = new ArrayList<>();
        ArrayList<Bullet> bulletsToRemoveEnemy = new ArrayList<>();
        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();
        ArrayList<Booster> boostersToRemove = new ArrayList<>();

        for (Bullet bulletPlayer : bulletsPlayer) {
            for (Bullet bulletEnemy : bulletsEnemy) {
                if (pixelPerfectCollision(bulletPlayer.bitmap, bulletPlayer.x, bulletPlayer.y,
                        bulletEnemy.bitmap, bulletEnemy.x, bulletEnemy.y)) {
                    if (!bulletsToRemovePlayer.contains(bulletPlayer)) {
                        bulletsToRemovePlayer.add(bulletPlayer);
                    }
                    if (!bulletsToRemoveEnemy.contains(bulletEnemy)) {
                        bulletsToRemoveEnemy.add(bulletEnemy);
                    }
                    break;
                }
            }
        }

        for (Bullet bullet : bulletsPlayer) {
            for (Enemy enemy : enemies) {
                if (pixelPerfectCollision(bullet.bitmap, bullet.x, bullet.y, enemy.bitmap, enemy.x, enemy.y)) {
                    if (!enemiesToRemove.contains(enemy)) {
                        enemiesToRemove.add(enemy);
                    }
                    if (!bulletsToRemovePlayer.contains(bullet)) {
                        bulletsToRemovePlayer.add(bullet);
                    }
                    break;
                }
            }
        }

        for (Bullet bullet : bulletsEnemy) {
            if (pixelPerfectCollision(bullet.bitmap, bullet.x, bullet.y, playerBitmap, playerX, playerY)) {
                gameOver = true;
                winner = "Enemigo";
                return;
            }
        }

        for (Booster booster : boosters) {
            if (pixelPerfectCollision(playerBitmap, playerX, playerY, booster.bitmap, booster.x, booster.y)) {
                playerAmmo += 20;
                if (!boostersToRemove.contains(booster)) {
                    boostersToRemove.add(booster);
                }
                break;
            }
        }

        bulletsPlayer.removeAll(bulletsToRemovePlayer);
        bulletsEnemy.removeAll(bulletsToRemoveEnemy);
        enemies.removeAll(enemiesToRemove);
        boosters.removeAll(boostersToRemove);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver) return true;

        int x = (int) event.getX(), y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x >= playerX && x <= playerX + playerBitmap.getWidth() && y >= playerY && y <= playerY + playerBitmap.getHeight()) {
                    if (playerAmmo > 0) {
                        int bulletX = playerX + playerBitmap.getWidth() / 2 - bulletPlayerBitmap.getWidth() / 2;
                        int bulletY = playerY + playerBitmap.getHeight() / 2 - bulletPlayerBitmap.getHeight() / 2;
                        bulletsPlayer.add(new Bullet(bulletX, bulletY, bulletPlayerBitmap));
                        playerAmmo--;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                playerY = Math.max(0, Math.min(getHeight() - playerBitmap.getHeight(), y - playerBitmap.getHeight() / 2));
                break;

            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }
}