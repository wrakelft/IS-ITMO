(function () {
    let socket = null;
    const listeners = new Set();

    function start() {
        if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) return;

        const proto = location.protocol === "https:" ? "wss:" : "ws:";
        const CONTEXT = location.pathname.split("/")[1];
        const wsUrl = `${proto}//${location.host}/${CONTEXT}/ws/organizations`;
        socket = new WebSocket(wsUrl);

        socket.onmessage = (e) => {
            let msg;
            try { msg = JSON.parse(e.data); } catch { return; }
            listeners.forEach(fn => fn(msg));
        };
    }

    function on(fn) {
        listeners.add(fn);
        return () => listeners.delete(fn);
    }

    window.wsBus = { start, on };
})();
