// Author: olshansky (c) 2026

import type { Tab } from "../App";

export default function Tabs({ tab, onChange }: { tab: Tab; onChange: (t: Tab) => void }) {
    return (
        <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
            <button onClick={() => onChange("logs")} disabled={tab === "logs"}>
                Логи
            </button>
            <button onClick={() => onChange("mappings")} disabled={tab === "mappings"}>
                Маппинги
            </button>
        </div>
    );
}
