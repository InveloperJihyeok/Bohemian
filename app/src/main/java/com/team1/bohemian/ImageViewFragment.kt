package com.team1.bohemian

// ImageViewFragment.kt
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class ImageViewFragment : Fragment() {

    private lateinit var imageUrl: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUrl = it.getParcelable(ARG_IMAGE_URI)!!
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_image_view, container, false)
        val imageView: ImageView = rootView.findViewById(R.id.imageView)

        // Glide를 사용하여 이미지 표시
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        return rootView
    }

    companion object {
        private const val ARG_IMAGE_URI = "imageUri"

        fun newInstance(imageUri: String): ImageViewFragment {
            val fragment = ImageViewFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE_URI, Uri.parse(imageUri))
            fragment.arguments = args
            return fragment
        }
    }
}
