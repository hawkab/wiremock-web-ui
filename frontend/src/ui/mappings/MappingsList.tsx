// Author: olshansky (c) 2026

export default function MappingsList({
    mappings,
    selectedId,
    onSelect,
}: {
    mappings: any[];
    selectedId: string | null;
    onSelect: (m: any) => void;
}) {
    return (
        <div style={{ border: "1px solid #333", borderRadius: 8, overflow: "hidden" }}>
            {mappings.map((m: any) => {
                const req = m?.request ?? {};
                const title = `${req.method ?? "?"} ${req.url ?? req.urlPattern ?? req.urlPath ?? req.urlPathPattern ?? "?"}`;
                const label = m?.name ? `${m.name}  |  ${title}` : title;
                const active = selectedId === m?.id;

                return (
                    <div
                        key={m.id}
                        onClick={() => onSelect(m)}
                        style={{
                            padding: 10,
                            cursor: "pointer",
                            borderBottom: "1px solid #222",
                            background: active ? "#1f1f1f" : "transparent",
                        }}
                    >
                        <div style={{ fontWeight: 600 }}>{label}</div>
                        <div className="mono" style={{ opacity: 0.7, fontSize: 12 }}>
                            {m.id}
                        </div>
                    </div>
                );
            })}
            {!mappings.length && <div style={{ padding: 10, opacity: 0.7 }}>Пусто</div>}
        </div>
    );
}
