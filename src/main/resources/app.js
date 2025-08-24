window.onload = () => {
    connect();
}

let stompClient = null;
let nodeRadius = 20;
const svgNS = "http://www.w3.org/2000/svg";
const svg = document.getElementById("tree-container");
let nodeMap = {};
let dataType;

function elements() {
    stompClient.send("/app/elements", {}, JSON.stringify());
}

function searchElement() {
    let searchElement = document.getElementById("searchElement");
    if (isNaN(searchElement.value)) {
        return;
    }
    stompClient.send("/app/profile-search", {}, searchElement.value);
    searchElement.value = '';
}

function insertProfile() {
    let insertSize = document.getElementById("insertSize");
    if (isNaN(insertSize.value)) {
        return;
    }
    document.getElementById("insertProfileResult").textContent = '';
    stompClient.send("/app/profile-insert", {}, insertSize.value);
    insertSize.value = '';
}

function insert() {
    let value = document.getElementById("insertValue").value;
    let type = document.getElementById("typeSelect").value;
    if (type === 'int') {
        value = parseInt(value, 10);
        if (isNaN(value)) {
            alert("Введите корректное целочисленное значение");
            return;
        }
    } else if (type === 'double') {
        value = parseFloat(value);
        if (isNaN(value)) {
            alert("Введите корректное число с плавающей точкой");
            return;
        }
    } else if (type === 'string') {
        if (value.length == 0) {
            alert("Введите корректное строковое значение");
            return;
        }
    } else {
        alert("Выберите тип данных");
        return;
    }
    dataType = type;
    if (dataType === 'string') {
        nodeRadius = 30;
    } else {
        nodeRadius = 20;
    }
    stompClient.send("/app/insert", {}, JSON.stringify({type, value}));
    document.getElementById("insertValue").value = '';
}

function reset() {
    stompClient.send("/app/reset", {});
    window.location.reload();
}

function connect() {
    console.log();
    const socket = new SockJS('http://193.233.23.160:8080/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {

        stompClient.subscribe('/process/compare', function (message) {
            const node = JSON.parse(message.body);
            console.log("Compare with:", node);
            drawArrows(node);
        });

        stompClient.subscribe('/process/found', function (message) {
            const node = JSON.parse(message.body);
            console.log("Found node:", node);
            stompClient.send("/app/pull", {});
        });

        stompClient.subscribe('/process/pull', function (response) {
            const tree = JSON.parse(response.body);
            console.log("Received full tree:", tree);
            drawTree(tree);
        });

        stompClient.subscribe('/process/insert-profile-result', function (response) {
            console.log("Received insert profile result:", response);
            document.getElementById("insertProfileResult").textContent = 'Вставка заняла ' + response.body + ' сек.';
        });

        stompClient.subscribe('/process/elements', function (response) {
            document.getElementById('elements').textContent = response.body;
        });

        stompClient.subscribe('/process/profile-search', function (response) {
            document.getElementById('element').textContent = 'Поиск занял ' + response.body + ' сек.';
        });

        stompClient.send("/app/pull", {});
    });
}

function offsetLine(x1, y1, x2, y2, radius) {
    const dx = x2 - x1;
    const dy = y2 - y1;
    const dist = Math.sqrt(dx * dx + dy * dy);
    if (dist === 0) return [x1, y1, x2, y2]; // защита от деления на 0

    const offsetX = (dx / dist) * radius;
    const offsetY = (dy / dist) * radius;

    return [x1 + offsetX, y1 + offsetY, x2 - offsetX, y2 - offsetY];
}

function drawArrowXY(x1, y1, x2, y2) {
    const [startX, startY, endX, endY] = offsetLine(x1, y1, x2, y2, nodeRadius);

    const line = document.createElementNS(svgNS, "line");
    line.setAttribute("x1", startX);
    line.setAttribute("y1", startY);
    line.setAttribute("x2", endX);
    line.setAttribute("y2", endY);
    line.setAttribute("stroke", "#96a1ff");
    line.setAttribute("stroke-width", "2");
    line.setAttribute("marker-end", "url(#arrowhead)");
    line.classList.add("animated-line");

    svg.appendChild(line);
}

function drawArrows(node) {
    const parent = nodeMap[node.value];
    if (!parent) return;

    if (node.left && nodeMap[node.left.value]) {
        drawArrowXY(parent.x, parent.y, nodeMap[node.left.value].x, nodeMap[node.left.value].y);
    }

    if (node.right && nodeMap[node.right.value]) {
        drawArrowXY(parent.x, parent.y, nodeMap[node.right.value].x, nodeMap[node.right.value].y);
    }

    parent.element.classList.add("highlight-compare");
    setTimeout(() => {
        parent.element.classList.remove("highlight-compare");
    }, 2000);
}

function drawTree(root) {
    svg.innerHTML = '';
    nodeMap = {};

    let nodeDistanceX;
    let nodeDistanceY;
    if (dataType === 'string') {
        nodeDistanceX = 120;
        nodeDistanceY = 120;
    } else {
        nodeDistanceX = 80
        nodeDistanceY = 80
    }

    // 1. Первый проход — считаем ширину поддерева, чтобы корректно рисовать X
    function calculateWidths(node) {
        if (!node) return 0;
        node.leftWidth = calculateWidths(node.left);
        node.rightWidth = calculateWidths(node.right);
        return Math.max(1, node.leftWidth + node.rightWidth);
    }

    // 2. Второй проход — рисуем
    function draw(node, x, y) {
        if (!node) return;

        drawNodeAt(node, x, y);

        // Вычисляем сдвиг на основе ширины поддеревьев
        if (node.left) {
            const leftX = x - nodeDistanceX * node.leftWidth;
            const leftY = y + nodeDistanceY;
            draw(node.left, leftX, leftY);
            drawArrowXY(x, y, leftX, leftY);
        }

        if (node.right) {
            const rightX = x + nodeDistanceX * node.rightWidth;
            const rightY = y + nodeDistanceY;
            draw(node.right, rightX, rightY);
            drawArrowXY(x, y, rightX, rightY);
        }
    }

    // 3. Вызываем оба прохода
    calculateWidths(root);
    draw(root, 200, 50);
    updateSvgSize();
}

function updateSvgSize() {
    const margin = 100;
    let maxX = 0, maxY = 0;

    Object.values(nodeMap).forEach(({x, y}) => {
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
    });

    svg.setAttribute("width", maxX + margin);
    svg.setAttribute("height", maxY + margin);
    svg.setAttribute("viewBox", `0 0 ${maxX + margin} ${maxY + margin}`);
}

function drawNodeAt(node, x, y) {
    const circle = document.createElementNS(svgNS, "circle");
    circle.setAttribute("cx", x);
    circle.setAttribute("cy", y);
    circle.setAttribute("r", nodeRadius);
    circle.setAttribute("class", "tree-node");

    const text = document.createElementNS(svgNS, "text");
    text.setAttribute("x", x);
    text.setAttribute("y", y + 5);
    text.setAttribute("text-anchor", "middle");
    text.textContent = node.value;

    svg.appendChild(circle);
    svg.appendChild(text);

    nodeMap[node.value] = {x, y, element: circle};
}