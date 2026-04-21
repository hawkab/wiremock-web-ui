// Author: olshansky (c) 2026

export default function LogsTable({
    items,
    selectedKey,
    onSelect,
    eventKey,
    fmtTime,
}: {
    items: any[];
    selectedKey: string | null;
    onSelect: (key: string) => void;
    eventKey: (it: any) => string;
    fmtTime: (it: any) => string;
}) {
    return (
        <div className="tableWrap">
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                    <tr style={{ textAlign: "left", borderBottom: "1px solid #222" }}>
                        <th style={{ padding: 10, width: 90 }}>Метод</th>
                        <th style={{ padding: 10 }}>URL</th>
                        <th style={{ padding: 10, width: 190 }}>Время</th>
                        <th style={{ padding: 10, width: 110 }}>Matched</th>
                    </tr>
                </thead>
                <tbody>
                    {items.map((it: any) => {
                        const req = it?.request ?? {};
                        const key = eventKey(it);
                        const active = key === selectedKey;

                        const matched = it?.wasMatched === true;

                        return (
                            <tr
                                key={key}
                                onClick={() => onSelect(key)}
                                style={{
                                    cursor: "pointer",
                                    borderBottom: "1px solid #222",
                                    background: active ? "#1f1f1f" : "transparent",
                                }}
                            >
                                <td style={{ padding: 10, fontWeight: 700 }}>{req.method ?? "?"}</td>
                                <td className="mono" style={{ padding: 10, fontSize: 13 }}>
                                    {req.url ?? req.absoluteUrl ?? "?"}
                                </td>
                                <td style={{ padding: 10, opacity: 0.85 }}>{fmtTime(it)}</td>
                                <td style={{ padding: 10 }}>{matched ? "✅ да" : "❌ нет"}</td>
                            </tr>
                        );
                    })}

                    {!items.length && (
                        <tr>
                            <td colSpan={4} style={{ padding: 10, opacity: 0.7 }}>
                                Пусто
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
}
