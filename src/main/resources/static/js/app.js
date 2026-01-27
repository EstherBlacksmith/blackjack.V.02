/**
 * Kawaii Blackjack - Application JavaScript
 * Following Separation of Concerns principle
 */

console.log("=== KAWAII BLACKJACK JS LOADED ===" + new Date().toISOString());

// Global state
let currentPlayerId = null;
let currentGameId = null;
let crupierTurnStarted = false;
let crupierPlayTimeout = null;

// Suit and rank mappings
const suitMap = {
    'HEARTS': { emoji: '♥️', symbol: '♥', isRed: true },
    'DIAMONDS': { emoji: '♦️', symbol: '♦', isRed: true },
    'SPADES': { emoji: '♠️', symbol: '♠', isRed: false },
    'CLUBS': { emoji: '♣️', symbol: '♣', isRed: false },
    '♥': { emoji: '♥️', symbol: '♥', isRed: true },
    '♦': { emoji: '♦️', symbol: '♦', isRed: true },
    '♠': { emoji: '♠️', symbol: '♠', isRed: false },
    '♣': { emoji: '♣️', symbol: '♣', isRed: false }
};

const rankDisplayMap = {
    'TWO': '2', 'THREE': '3', 'FOUR': '4', 'FIVE': '5',
    'SIX': '6', 'SEVEN': '7', 'EIGHT': '8', 'NINE': '9',
    'TEN': '10', 'JACK': 'J', 'QUEEN': 'Q', 'KING': 'K', 'ACE': 'A'
};

// ==================== Event Listeners ====================

document.addEventListener('DOMContentLoaded', function() {
    setupButtonListeners();
    setupModalCloseListeners();
});

function setupButtonListeners() {
    const btnStand = document.getElementById('btnStand');
    if (btnStand) {
        btnStand.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            window.standFunction();
        });
    }
    
    const btnHit = document.getElementById('btnHit');
    if (btnHit) {
        btnHit.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            window.hitFunction();
        });
    }
}

 function setupModalCloseListeners() {
     document.addEventListener('click', function(event) {
         const rankingModal = document.getElementById("rankingModal");
         const gameDetailsModal = document.getElementById("gameDetailsModal");

         if (event.target === rankingModal) {
             closeRanking();
         }
         if (event.target === gameDetailsModal) {
             closeGameDetails();
         }
     });
 }


// ==================== Game Actions ====================

window.standFunction = async function() {
    if (!currentGameId) {
        alert("No active game");
        return;
    }
    
    try {
        const response = await fetch("/games/" + currentGameId + "/stand", { method: "POST" });
        
        if (response.ok) {
            const game = await response.json();
            updateGameDisplay(game);
        } else if (response.status === 400) {
            const error = await response.json();
            document.getElementById("resultDisplay").innerHTML = 
                "<div class=\"result\" style=\"color: #ff6b9d\">" + (error.message || "Cannot stand now") + "</div>";
        } else {
            alert("Error standing: " + response.status);
        }
    } catch (error) {
        console.error("Stand exception:", error);
        alert("Connection error while standing");
    }
};

window.hitFunction = async function() {
    if (!currentGameId) {
        alert("No active game");
        return;
    }
    
    const btnHit = document.getElementById("btnHit");
    const btnStand = document.getElementById("btnStand");
    if (btnHit) btnHit.disabled = true;
    if (btnStand) btnStand.disabled = true;
    
    try {
        const response = await fetch("/games/" + currentGameId + "/hit", { method: "POST" });
        
        if (response.ok) {
            const game = await response.json();
            updateGameDisplay(game);
            
            if (game.result && game.result !== "NO_RESULTS_YET") {
                loginOrCreate();
            }
        } else {
            if (btnHit) btnHit.disabled = false;
            if (btnStand) btnStand.disabled = false;
        }
    } catch (error) {
        console.error("Hit exception:", error);
        if (btnHit) btnHit.disabled = false;
        if (btnStand) btnStand.disabled = false;
    }
};

// ==================== Player Management ====================

async function loginOrCreate() {
    const name = document.getElementById("playerName").value;
    if (!name) return alert("Please enter your name");
    
    const response = await fetch("/players/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: name })
    });
    
    if (response.ok) {
        const player = await response.json();
        currentPlayerId = player.id;
        
        document.getElementById("playerIdDisplay").innerHTML = 
            "👤 " + player.name + " (ID: " + player.id + ") 🐱";
        
        document.getElementById("statWins").textContent = player.wins;
        document.getElementById("statLosses").textContent = player.losses;
        document.getElementById("statPushes").textContent = player.pushes;
        
        updateWinRate(player.wins, player.losses, player.pushes);
        loadPlayerStats();
        
        document.getElementById("playerInfo").classList.remove("hidden");
        document.getElementById("startSection").classList.remove("hidden");
    }
}

async function loadPlayerStats() {
    if (!currentPlayerId) return;
    
    try {
        const response = await fetch("/players/" + currentPlayerId + "/stats");
        if (response.ok) {
            const stats = await response.json();
            console.log("Stats loaded:", stats);
            document.getElementById("statWins").textContent = stats.wins;
            document.getElementById("statLosses").textContent = stats.losses;
            document.getElementById("statPushes").textContent = stats.pushes;
            updateWinRate(stats.wins, stats.losses, stats.pushes);
            updateHistoryTable(stats.recentGames || []);
        } else {
            console.log("Stats endpoint returned:", response.status);
        }
    } catch (error) {
        console.log("Could not load stats:", error.message);
    }
}

// ==================== Statistics & History ====================

function updateWinRate(wins, losses, pushes) {
    const total = wins + losses + pushes;
    const winRate = total > 0 ? Math.round((wins / total) * 100) : 0;
    document.getElementById("winRateDisplay").textContent = winRate + "%";
    document.getElementById("winRateFill").style.width = winRate + "%";
}

function updateHistoryTable(recentGames) {
    const historyList = document.getElementById("historyList");
    
    console.log("Updating history table with:", recentGames);
    
    if (!recentGames || recentGames.length === 0) {
        historyList.innerHTML = 
            '<tr><td colspan="5" style="text-align: center; color: #999;">No games yet 🐱</td></tr>';
        return;
    }
    
    const historyHtml = recentGames.map(function(game) {
        let resultClass = "";
        let resultText = "";
        
        if (game.result === "PLAYER_WINS") {
            resultClass = "history-win";
            resultText = "Win 🏆";
        } else if (game.result === "CRUPIER_WINS") {
            resultClass = "history-loss";
            resultText = "Loss ❌";
        } else {
            resultClass = "history-push";
            resultText = "Push 🤝";
        }
        
        let dateStr = "Recently";
        if (game.playedAt && game.playedAt !== "N/A") {
            try {
                dateStr = game.playedAt;
            } catch (e) {
                dateStr = "Recently";
            }
        }
        
        return '<tr class="' + resultClass + '">' +
            '<td><span class="history-result ' + 
                (game.result === "PLAYER_WINS" ? "win" : (game.result === "CRUPIER_WINS" ? "loss" : "push")) + 
                '">' + resultText + '</span></td>' +
            '<td>' + game.playerScore + '</td>' +
            '<td>' + game.dealerScore + '</td>' +
            '<td>' + dateStr + '</td>' +
            '<td>' +
                '<button onclick="viewGameDetails(\'' + game.gameId + '\')" ' +
                    'style="padding: 4px 8px; font-size: 12px; margin-right: 4px;">👁️</button>' +
                '<button onclick="deleteGameFromHistory(\'' + game.gameId + '\')" ' +
                    'style="padding: 4px 8px; font-size: 12px;">🗑️</button>' +
            '</td>' +
            '</tr>';
    }).join("");
    
    historyList.innerHTML = historyHtml;
}

async function deleteGameFromHistory(gameId) {
    if (!confirm("Are you sure you want to delete this game?")) return;
    
    try {
        const response = await fetch("/games/" + gameId + "/delete", { method: "DELETE" });
        
        if (response.ok) {
            console.log("Game deleted successfully");
            loadPlayerStats();
        } else {
            alert("Error deleting game");
        }
    } catch (error) {
        console.error("Delete error:", error);
        alert("Error deleting game: " + error.message);
    }
}

// ==================== Game Lifecycle ====================

async function startGame() {
    if (!currentPlayerId) return alert("Please create a player first");
    
    clearGameTimeouts();
    window.crupierTurnStarted = false;
    
    const response = await fetch("/games/new", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ playerId: currentPlayerId })
    });
    
    if (response.ok) {
        const game = await response.json();
        currentGameId = game.id;
        document.getElementById("gameIdDisplay").textContent = game.id;
        document.getElementById("gameSection").classList.remove("hidden");
        updateGameDisplay(game);
    }
}

async function newGame() {
    clearGameTimeouts();
    window.crupierTurnStarted = false;
    
    document.getElementById("btnHit").classList.remove("hidden");
    document.getElementById("btnStand").classList.remove("hidden");
    document.getElementById("btnNewGame").classList.add("hidden");
    document.getElementById("resultDisplay").innerHTML = "";
    document.getElementById("dealerIndicator").classList.add("hidden");
    startGame();
}

async function deleteGame() {
    if (!currentGameId) return;
    
    clearGameTimeouts();
    
    const btnDelete = document.getElementById("btnDelete");
    btnDelete.disabled = true;
    btnDelete.textContent = "Deleting...";
    
    try {
        const response = await fetch("/games/" + currentGameId + "/delete", { method: "DELETE" });
        
        if (response.ok) {
            currentGameId = null;
            document.getElementById("gameSection").classList.add("hidden");
        } else {
            alert("Error deleting game");
        }
    } catch (error) {
        alert("Error deleting game: " + error.message);
    } finally {
        btnDelete.disabled = false;
        btnDelete.textContent = "🗑️ Delete";
    }
}

function clearGameTimeouts() {
    if (crupierPlayTimeout) {
        clearTimeout(crupierPlayTimeout);
        crupierPlayTimeout = null;
    }
}

// ==================== Game Display ====================

function createCardElement(card) {
    console.log("Creating card from:", card);
    
    let rank = card.rank || 'A';
    let suit = card.suit || 'SPADES';
    
    const suitInfo = suitMap[suit.toUpperCase()] || suitMap['SPADES'];
    const isRed = suitInfo.isRed;
    const suitEmoji = suitInfo.emoji;
    
    const rankDisplay = rankDisplayMap[rank.toUpperCase()] || rank.charAt(0);
    
    return '<div class="card ' + (isRed ? 'red' : 'black') + '">' +
        '<span class="corner corner-top">' + rankDisplay + '</span>' +
        '<span class="center">' + suitEmoji + '</span>' +
        '<span class="corner corner-bottom">' + rankDisplay + '</span>' +
        '</div>';
}

function updateGameDisplay(game) {
    console.log("Updating game display:", game);
    
    document.getElementById("resultDisplay").innerHTML = "";
    
    const playerCardsHtml = game.player.hand.map(function(c) { 
        return createCardElement(c); 
    }).join("");
    document.getElementById("playerCards").innerHTML = playerCardsHtml;
    document.getElementById("playerScore").textContent = game.player.score;
    
    const dealerCardsHtml = game.crupierHand.map(function(c) { 
        return createCardElement(c); 
    }).join("");
    document.getElementById("dealerCards").innerHTML = dealerCardsHtml;
    document.getElementById("dealerScore").textContent = game.crupierScore;
    
    const dealerIndicator = document.getElementById("dealerIndicator");
    if (game.status === "CRUPIER_TURN") {
        dealerIndicator.classList.remove("hidden");
    } else {
        dealerIndicator.classList.add("hidden");
    }
    
    handleGameStatus(game, dealerIndicator);
}

function handleGameStatus(game, dealerIndicator) {
    if (game.status === "CRUPIER_TURN" && !window.crupierTurnStarted) {
        window.crupierTurnStarted = true;
        crupierPlayStep();
    } else if (game.status !== "CRUPIER_TURN") {
        window.crupierTurnStarted = false;
    }
    
    const btnHit = document.getElementById("btnHit");
    const btnStand = document.getElementById("btnStand");
    const btnNewGame = document.getElementById("btnNewGame");
    
    const isFinalResult = game.result && game.result !== "NO_RESULTS_YET";
    
    if (isFinalResult) {
        handleFinalResult(game, dealerIndicator, btnHit, btnStand, btnNewGame);
    } else {
        handleActiveGame(game, btnHit, btnStand, btnNewGame);
    }
}

function handleFinalResult(game, dealerIndicator, btnHit, btnStand, btnNewGame) {
    dealerIndicator.classList.add("hidden");
    window.crupierTurnStarted = false;
    clearGameTimeouts();
    
    const resultMap = {
        "PLAYER_WINS": "YOU WIN! 🎉 🐱",
        "CRUPIER_WINS": "YOU LOSE 😔",
        "PUSH": "PUSH 🤝",
        "BLACKJACK": "BLACKJACK! 🏆 ✨"
    };
    
    const displayResult = resultMap[game.result] || game.result;
    const colors = {
        "YOU WIN! 🎉 🐱": "#84fab0",
        "YOU LOSE 😔": "#ff9a9e",
        "PUSH 🤝": "#f6d365",
        "BLACKJACK! 🏆 ✨": "#a18cd1"
    };
    
    document.getElementById("resultDisplay").innerHTML = 
        "<div class=\"result\" style=\"color: " + colors[displayResult] + "\">" + displayResult + "</div>";
    
    btnHit.classList.add("hidden");
    btnStand.classList.add("hidden");
    btnNewGame.classList.remove("hidden");
    btnHit.disabled = true;
    btnStand.disabled = true;
}

function handleActiveGame(game, btnHit, btnStand, btnNewGame) {
    const playerStatus = game.player.status;
    const canHit = playerStatus === "ACTIVE";
    const canStand = playerStatus === "ACTIVE";
    
    btnHit.classList.remove("hidden");
    btnStand.classList.remove("hidden");
    btnNewGame.classList.add("hidden");
    
    btnHit.disabled = !canHit;
    btnStand.disabled = !canStand;
}

// ==================== Crupier (Dealer) AI ====================

function crupierPlayStep() {
    if (!currentGameId) return;
    if (window.crupierTurnStarted === false) return;
    
    console.log("Dealer playing... gameId:", currentGameId);
    
    crupierPlayTimeout = setTimeout(async function() {
        try {
            const response = await fetch("/games/" + currentGameId + "/crupier-hit", {
                method: "POST"
            });
            
            if (response.ok) {
                const game = await response.json();
                updateGameDisplay(game);
                
                if (game.status === "FINISHED" && game.result && game.result !== "NO_RESULTS_YET") {
                    loginOrCreate();
                }
                
                if (game.status === "CRUPIER_TURN") {
                    crupierPlayStep();
                }
            } else {
                console.error("Dealer hit failed:", response.status);
            }
        } catch (error) {
            console.error("Dealer hit exception:", error);
        }
    }, 800);
}

// ==================== Ranking Feature ====================

async function showRanking() {
    const modal = document.getElementById("rankingModal");
    const rankingList = document.getElementById("rankingList");
    
    rankingList.innerHTML = 
        '<tr><td colspan="4" style="text-align: center;">Loading... 🐱</td></tr>';
    modal.classList.add("show");
    
    try {
        const response = await fetch("/players/ranking");
        if (response.ok) {
            const rankings = await response.json();
            console.log("Rankings loaded:", rankings);
            
            if (!rankings || rankings.length === 0) {
                rankingList.innerHTML = 
                    '<tr><td colspan="4" style="text-align: center; color: #999;">No players yet 🐱</td></tr>';
                return;
            }
            
            const rankingHtml = rankings.map(function(player) {
                let positionClass = "";
                let medal = "";
                
                if (player.rank === 1) {
                    positionClass = "gold";
                    medal = "🥇";
                } else if (player.rank === 2) {
                    positionClass = "silver";
                    medal = "🥈";
                } else if (player.rank === 3) {
                    positionClass = "bronze";
                    medal = "🥉";
                }
                
                return '<tr>' +
                    '<td><span class="ranking-position ' + positionClass + '">' + 
                        medal + ' #' + player.rank + '</span></td>' +
                    '<td><strong>' + escapeHtml(player.playerName) + '</strong></td>' +
                    '<td style="color: #84fab0; font-weight: bold;">' + player.wins + ' 🏆</td>' +
                    '<td style="color: #ff9a9e;">' + player.losses + ' 💔</td>' +
                    '</tr>';
            }).join("");
            
            rankingList.innerHTML = rankingHtml;
        } else {
            rankingList.innerHTML = 
                '<tr><td colspan="4" style="text-align: center; color: #ff9a9e;">Error loading rankings</td></tr>';
        }
    } catch (error) {
        console.error("Ranking error:", error);
        rankingList.innerHTML = 
            '<tr><td colspan="4" style="text-align: center; color: #ff9a9e;">Connection error</td></tr>';
    }
}

function closeRanking() {
    document.getElementById("rankingModal").classList.remove("show");
}

// ==================== Utility Functions ====================

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}// ==================== Game Details Feature ====================

 async function viewGameDetails(gameId) {
     const modal = document.getElementById("gameDetailsModal");
     const content = document.getElementById("gameDetailsContent");

     content.innerHTML = '<p>Loading game details... 🐱</p>';
     modal.classList.add("show");

     try {
         const response = await fetch("/games/" + gameId);

         if (response.ok) {
             const game = await response.json();
             displayGameDetails(game);
         } else if (response.status === 404) {
             content.innerHTML = '<p style="color: #ff9a9e;">Game not found 😔</p>';
         } else {
             content.innerHTML = '<p style="color: #ff9a9e;">Error loading game details</p>';
         }
     } catch (error) {
         console.error("Error loading game details:", error);
         content.innerHTML = '<p style="color: #ff9a9e;">Connection error</p>';
     }
 }

 function displayGameDetails(game) {
     const content = document.getElementById("gameDetailsContent");

     // Format result
     let resultText = game.result || "N/A";
     if (game.result === "PLAYER_WINS") resultText = "WIN 🏆";
     else if (game.result === "CRUPIER_WINS") resultText = "LOSE ❌";
     else if (game.result === "PUSH") resultText = "PUSH 🤝";
     else if (game.result === "BLACKJACK") resultText = "BLACKJACK! ✨";

     // Create player cards HTML
     const playerCardsHtml = game.player.hand.map(function(card) {
         return createCardElement(card);
     }).join("");

     // Create dealer cards HTML
     const dealerCardsHtml = game.crupierHand.map(function(card) {
         return createCardElement(card);
     }).join("");

     content.innerHTML =
         '<div class="game-details-info">' +
         '<p><strong>Game ID:</strong> ' + game.id.substring(0, 8) + '...</p>' +
         '<p><strong>Status:</strong> ' + game.status + '</p>' +
         '<p><strong>Result:</strong> ' + resultText + '</p>' +
         '</div>' +
         '<div class="game-details-hands">' +
         '<div class="hand-section">' +
         '<h4>🃏 Your Hand (Score: ' + game.player.score + ')</h4>' +
         '<div class="cards-container small-cards">' + playerCardsHtml + '</div>' +
         '</div>' +
         '<div class="hand-section">' +
         '<h4>🎰 Dealer Hand (Score: ' + game.crupierScore + ')</h4>' +
         '<div class="cards-container small-cards">' + dealerCardsHtml + '</div>' +
         '</div>' +
         '</div>';
 }

 function closeGameDetails() {
     document.getElementById("gameDetailsModal").classList.remove("show");
 }



// Export functions to window for HTML onclick handlers
window.hit = async function() { return window.hitFunction(); };
window.stand = async function() { return window.standFunction(); };
