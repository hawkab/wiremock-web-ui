// Author: olshansky (c) 2026

export default function LogsToolbar({
    query,
    onQueryChange,
    onRefresh,
}: {
    query: string;
    onQueryChange: (v: string) => void;
    onRefresh: () => void;
}) {
    return (
        <div style={{ display: "flex", gap: 8 }}>
            <button onClick={onRefresh}>Обновить</button>
            <input
                value={query}
                onChange={(e) => onQueryChange(e.target.value)}
                placeholder="Поиск по логам"
                style={{ flex: 1 }}
            />
        </div>
    );
}
