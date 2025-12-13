import { useEffect, useRef, useState } from "react"

function SearchBar() {
    const [query, setQuery] = useState("")
    const [results, setResults] = useState<string[]>([])
    const [loading, setLoading] = useState(false)

    const abortRef = useRef<AbortController | null>(null)
    const debounceRef = useRef<number | null>(null)

    const search = async () => {
        abortRef.current?.abort()
        abortRef.current = new AbortController()

        try {
            setLoading(true)
            const res = await fetch(
                `http://localhost:8080/query?query=${encodeURIComponent(query)}`,
                { signal: abortRef.current.signal }
            )

            if (!res.ok) throw new Error("Request failed")

            const data = await res.json()
            setResults(data.data ?? [])
        } catch (err: any) {
            if (err.name !== "AbortError") {
                console.error(err)
            }
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (debounceRef.current) {
            clearTimeout(debounceRef.current)
        }

        debounceRef.current = window.setTimeout(() => {
            search()
        }, 200)

        return () => {
            if (debounceRef.current) clearTimeout(debounceRef.current)
        }
    }, [query])

    const onKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            if (debounceRef.current) {
                clearTimeout(debounceRef.current)
            }
            search()
        }
    }

    return (
        <div className="w-full max-w-md mx-auto">
            <div className="flex gap-2">
                <input
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    onKeyDown={onKeyDown}
                    placeholder="Search..."
                    className="flex-1 rounded border px-3 py-2 focus:outline-none focus:ring"
                />

                <button
                    onClick={search}
                    disabled={loading}
                    className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
                >
                    Search
                </button>
            </div>

            {loading && (
                <div className="mt-2 text-sm text-gray-500">Loading...</div>
            )}

            {results.length > 0 && (
                <ul className="mt-2 rounded border bg-white shadow">
                    {results.map((item, idx) => (
                        <li
                            key={idx}
                            className="px-3 py-2 hover:bg-gray-100 cursor-pointer"
                        >
                            {item}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    )
}

export default SearchBar
