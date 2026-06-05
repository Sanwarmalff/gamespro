const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const scoreElement = document.getElementById('score');
const restartBtn = document.getElementById('restartBtn');

// Game variables
let score = 0;
let gameLoop;
let gameActive = true;
let frameCount = 0;
let speedMultiplier = 1;

// Player car properties
const player = {
    x: 130,
    y: 400,
    width: 40,
    height: 70,
    color: '#03DAC6'
};

// Enemy cars array
let enemies = [];

// Controls
let isTouchingLeft = false;
let isTouchingRight = false;

// Touch Event Listeners for Mobile
document.addEventListener('touchstart', (e) => {
    const touchX = e.touches[0].clientX;
    const screenWidth = window.innerWidth;
    
    // Left half of screen moves left, right half moves right
    if (touchX < screenWidth / 2) {
        isTouchingLeft = true;
    } else {
        isTouchingRight = true;
    }
}, { passive: false });

document.addEventListener('touchend', () => {
    isTouchingLeft = false;
    isTouchingRight = false;
});

// Prevent scrolling
document.body.addEventListener("touchmove", (e) => {
    e.preventDefault();
}, { passive: false });

function spawnEnemy() {
    const laneWidth = canvas.width / 3;
    const lane = Math.floor(Math.random() * 3);
    const enemyX = (lane * laneWidth) + (laneWidth / 2) - 20; // Center in lane
    
    enemies.push({
        x: enemyX,
        y: -80,
        width: 40,
        height: 70,
        speed: 3 + Math.random() * 2 + speedMultiplier,
        color: '#FF2E93'
    });
}

function update() {
    if (!gameActive) return;

    // Movement logic
    if (isTouchingLeft && player.x > 0) {
        player.x -= 5;
    }
    if (isTouchingRight && player.x < canvas.width - player.width) {
        player.x += 5;
    }

    // Increase difficulty over time
    frameCount++;
    if (frameCount % 60 === 0) {
        score += 10;
        scoreElement.innerText = score;
        if (score % 100 === 0) {
            speedMultiplier += 0.5; // Cars fall faster
        }
    }

    // Spawn new enemies
    if (frameCount % Math.max(30, 80 - (score / 10)) === 0) {
        spawnEnemy();
    }

    // Update enemies and check collisions
    for (let i = 0; i < enemies.length; i++) {
        enemies[i].y += enemies[i].speed;

        // Collision Detection (AABB)
        if (player.x < enemies[i].x + enemies[i].width &&
            player.x + player.width > enemies[i].x &&
            player.y < enemies[i].y + enemies[i].height &&
            player.y + player.height > enemies[i].y) {
            
            gameOver();
            return;
        }

        // Remove enemies that pass the bottom
        if (enemies[i].y > canvas.height) {
            enemies.splice(i, 1);
            i--;
        }
    }

    draw();
    gameLoop = requestAnimationFrame(update);
}

function draw() {
    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Draw lane dividers
    ctx.strokeStyle = '#555';
    ctx.setLineDash([20, 20]);
    ctx.beginPath();
    ctx.moveTo(canvas.width / 3, 0);
    ctx.lineTo(canvas.width / 3, canvas.height);
    ctx.moveTo((canvas.width / 3) * 2, 0);
    ctx.lineTo((canvas.width / 3) * 2, canvas.height);
    ctx.stroke();

    // Draw Player
    ctx.fillStyle = player.color;
    ctx.fillRect(player.x, player.y, player.width, player.height);

    // Draw Enemies
    for (let enemy of enemies) {
        ctx.fillStyle = enemy.color;
        ctx.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
    }
}

function gameOver() {
    gameActive = false;
    cancelAnimationFrame(gameLoop);
    restartBtn.style.display = 'block';
}

restartBtn.addEventListener('click', () => {
    score = 0;
    scoreElement.innerText = score;
    frameCount = 0;
    speedMultiplier = 1;
    enemies = [];
    player.x = 130;
    gameActive = true;
    restartBtn.style.display = 'none';
    requestAnimationFrame(update);
});

// Start the game loop
requestAnimationFrame(update);
