const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const scoreElement = document.getElementById('score');
const restartBtn = document.getElementById('restartBtn');

const grid = 20;
let count = 0;
let score = 0;
let gameLoop;

let snake = {
    x: 160,
    y: 160,
    dx: grid,
    dy: 0,
    cells: [],
    maxCells: 4
};

let food = {
    x: 320,
    y: 320
};

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}

function resetGame() {
    snake.x = 160;
    snake.y = 160;
    snake.cells = [];
    snake.maxCells = 4;
    snake.dx = grid;
    snake.dy = 0;
    score = 0;
    scoreElement.innerText = score;
    food.x = getRandomInt(0, 20) * grid;
    food.y = getRandomInt(0, 20) * grid;
    restartBtn.style.display = 'none';
    requestAnimationFrame(loop);
}

function loop() {
    gameLoop = requestAnimationFrame(loop);

    // Slow down the game loop to roughly 15 frames per second
    if (++count < 4) {
        return;
    }
    count = 0;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    snake.x += snake.dx;
    snake.y += snake.dy;

    // Wrap snake position horizontally on edge of screen
    if (snake.x < 0) {
        snake.x = canvas.width - grid;
    } else if (snake.x >= canvas.width) {
        snake.x = 0;
    }

    // Wrap snake position vertically on edge of screen
    if (snake.y < 0) {
        snake.y = canvas.height - grid;
    } else if (snake.y >= canvas.height) {
        snake.y = 0;
    }

    snake.cells.unshift({x: snake.x, y: snake.y});

    if (snake.cells.length > snake.maxCells) {
        snake.cells.pop();
    }

    // Draw food
    ctx.fillStyle = '#03DAC6';
    ctx.fillRect(food.x, food.y, grid - 1, grid - 1);

    // Draw snake
    ctx.fillStyle = '#6C63FF';
    snake.cells.forEach(function(cell, index) {
        ctx.fillRect(cell.x, cell.y, grid - 1, grid - 1);

        // Snake ate food
        if (cell.x === food.x && cell.y === food.y) {
            snake.maxCells++;
            score += 10;
            scoreElement.innerText = score;
            food.x = getRandomInt(0, 20) * grid;
            food.y = getRandomInt(0, 20) * grid;
        }

        // Check collision with all cells after this one
        for (let i = index + 1; i < snake.cells.length; i++) {
            if (cell.x === snake.cells[i].x && cell.y === snake.cells[i].y) {
                cancelAnimationFrame(gameLoop);
                restartBtn.style.display = 'block';
            }
        }
    });
}

// Touch controls for mobile Android WebView
let touchStartX = 0;
let touchStartY = 0;

document.addEventListener('touchstart', function(e) {
    touchStartX = e.changedTouches[0].screenX;
    touchStartY = e.changedTouches[0].screenY;
}, false);

document.addEventListener('touchend', function(e) {
    let touchEndX = e.changedTouches[0].screenX;
    let touchEndY = e.changedTouches[0].screenY;
    handleSwipe(touchStartX, touchStartY, touchEndX, touchEndY);
}, false);

function handleSwipe(startX, startY, endX, endY) {
    let dx = endX - startX;
    let dy = endY - startY;

    if (Math.abs(dx) > Math.abs(dy)) {
        // Horizontal swipe
        if (dx > 0 && snake.dx === 0) {
            snake.dx = grid;
            snake.dy = 0;
        } else if (dx < 0 && snake.dx === 0) {
            snake.dx = -grid;
            snake.dy = 0;
        }
    } else {
        // Vertical swipe
        if (dy > 0 && snake.dy === 0) {
            snake.dy = grid;
            snake.dx = 0;
        } else if (dy < 0 && snake.dy === 0) {
            snake.dy = -grid;
            snake.dx = 0;
        }
    }
}

// Prevent default scrolling when swiping on the canvas
document.body.addEventListener("touchmove", function(e) {
    e.preventDefault();
}, { passive: false });

restartBtn.addEventListener('click', function() {
    resetGame();
});

// Start the game
requestAnimationFrame(loop);
